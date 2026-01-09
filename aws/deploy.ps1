# DealHarbor Backend - AWS Deployment Script for Windows
# This script builds and deploys the application to AWS ECS

# Configuration - Update these values
$AWS_REGION = if ($env:AWS_REGION) { $env:AWS_REGION } else { "ap-south-1" }
$AWS_ACCOUNT_ID = if ($env:AWS_ACCOUNT_ID) { $env:AWS_ACCOUNT_ID } else { "YOUR_ACCOUNT_ID" }
$ECR_REPOSITORY = "dealharbor-backend-production"
$ECS_CLUSTER = "dealharbor-cluster-production"
$ECS_SERVICE = "dealharbor-service"
$IMAGE_TAG = if ($env:IMAGE_TAG) { $env:IMAGE_TAG } else { "latest" }

Write-Host "========================================" -ForegroundColor Green
Write-Host "DealHarbor Backend - AWS Deployment" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Check if AWS CLI is installed
if (!(Get-Command aws -ErrorAction SilentlyContinue)) {
    Write-Host "AWS CLI is not installed. Please install it first." -ForegroundColor Red
    exit 1
}

# Check if Docker is installed
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker is not installed. Please install it first." -ForegroundColor Red
    exit 1
}

try {
    # Step 1: Authenticate with ECR
    Write-Host "Step 1: Authenticating with Amazon ECR..." -ForegroundColor Yellow
    $password = aws ecr get-login-password --region $AWS_REGION
    $password | docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
    Write-Host "✓ ECR authentication successful" -ForegroundColor Green

    # Step 2: Build the Docker image
    Write-Host "Step 2: Building Docker image..." -ForegroundColor Yellow
    docker build -t "${ECR_REPOSITORY}:${IMAGE_TAG}" .
    if ($LASTEXITCODE -ne 0) { throw "Docker build failed" }
    Write-Host "✓ Docker image built successfully" -ForegroundColor Green

    # Step 3: Tag the image for ECR
    Write-Host "Step 3: Tagging image for ECR..." -ForegroundColor Yellow
    docker tag "${ECR_REPOSITORY}:${IMAGE_TAG}" "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"
    Write-Host "✓ Image tagged successfully" -ForegroundColor Green

    # Step 4: Push to ECR
    Write-Host "Step 4: Pushing image to ECR..." -ForegroundColor Yellow
    docker push "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"
    if ($LASTEXITCODE -ne 0) { throw "Docker push failed" }
    Write-Host "✓ Image pushed to ECR successfully" -ForegroundColor Green

    # Step 5: Update ECS Service
    Write-Host "Step 5: Updating ECS service..." -ForegroundColor Yellow
    aws ecs update-service `
        --cluster $ECS_CLUSTER `
        --service $ECS_SERVICE `
        --force-new-deployment `
        --region $AWS_REGION
    if ($LASTEXITCODE -ne 0) { throw "ECS service update failed" }
    Write-Host "✓ ECS service update triggered" -ForegroundColor Green

    # Step 6: Wait for deployment to complete
    Write-Host "Step 6: Waiting for deployment to stabilize..." -ForegroundColor Yellow
    aws ecs wait services-stable `
        --cluster $ECS_CLUSTER `
        --services $ECS_SERVICE `
        --region $AWS_REGION
    Write-Host "✓ Deployment completed successfully" -ForegroundColor Green

    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Deployment Complete!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
