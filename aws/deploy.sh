#!/bin/bash

# DealHarbor Backend - AWS Deployment Script
# This script builds and deploys the application to AWS ECS

set -e

# Configuration - Update these values
AWS_REGION="${AWS_REGION:-ap-south-1}"
AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-YOUR_ACCOUNT_ID}"
ECR_REPOSITORY="dealharbor-backend-production"
ECS_CLUSTER="dealharbor-cluster-production"
ECS_SERVICE="dealharbor-service"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}DealHarbor Backend - AWS Deployment${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}AWS CLI is not installed. Please install it first.${NC}"
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed. Please install it first.${NC}"
    exit 1
fi

# Step 1: Authenticate with ECR
echo -e "${YELLOW}Step 1: Authenticating with Amazon ECR...${NC}"
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
echo -e "${GREEN}✓ ECR authentication successful${NC}"

# Step 2: Build the Docker image
echo -e "${YELLOW}Step 2: Building Docker image...${NC}"
docker build -t $ECR_REPOSITORY:$IMAGE_TAG .
echo -e "${GREEN}✓ Docker image built successfully${NC}"

# Step 3: Tag the image for ECR
echo -e "${YELLOW}Step 3: Tagging image for ECR...${NC}"
docker tag $ECR_REPOSITORY:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG
echo -e "${GREEN}✓ Image tagged successfully${NC}"

# Step 4: Push to ECR
echo -e "${YELLOW}Step 4: Pushing image to ECR...${NC}"
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG
echo -e "${GREEN}✓ Image pushed to ECR successfully${NC}"

# Step 5: Update ECS Service
echo -e "${YELLOW}Step 5: Updating ECS service...${NC}"
aws ecs update-service \
    --cluster $ECS_CLUSTER \
    --service $ECS_SERVICE \
    --force-new-deployment \
    --region $AWS_REGION
echo -e "${GREEN}✓ ECS service update triggered${NC}"

# Step 6: Wait for deployment to complete
echo -e "${YELLOW}Step 6: Waiting for deployment to stabilize...${NC}"
aws ecs wait services-stable \
    --cluster $ECS_CLUSTER \
    --services $ECS_SERVICE \
    --region $AWS_REGION
echo -e "${GREEN}✓ Deployment completed successfully${NC}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
