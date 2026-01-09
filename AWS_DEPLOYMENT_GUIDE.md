# DealHarbor Backend - AWS Deployment Guide

This guide provides step-by-step instructions for deploying the DealHarbor Backend to AWS using ECS Fargate.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Architecture Overview](#architecture-overview)
3. [Step-by-Step Deployment](#step-by-step-deployment)
4. [CI/CD Setup](#cicd-setup)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before starting, ensure you have:

1. **AWS Account** with appropriate permissions
2. **AWS CLI** installed and configured
3. **Docker Desktop** installed
4. **Git** installed

### Install AWS CLI (Windows)

```powershell
# Download and install from:
# https://awscli.amazonaws.com/AWSCLIV2.msi

# Configure AWS CLI
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region (ap-south-1), Output format (json)
```

### Install Docker Desktop
Download from: https://www.docker.com/products/docker-desktop/

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
│  ┌─────────────┐                                                │
│  │   Route 53  │ ──── DNS                                       │
│  └──────┬──────┘                                                │
│         │                                                        │
│  ┌──────▼──────┐                                                │
│  │     ALB     │ ──── Application Load Balancer                 │
│  └──────┬──────┘                                                │
│         │                                                        │
│  ┌──────▼──────────────────────────────────────────────┐        │
│  │                    ECS Fargate                       │        │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │        │
│  │  │   Task 1    │  │   Task 2    │  │   Task N    │  │        │
│  │  │   (8080)    │  │   (8080)    │  │   (8080)    │  │        │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │        │
│  └─────────────────────────────────────────────────────┘        │
│         │                    │                                   │
│  ┌──────▼──────┐      ┌──────▼──────┐                           │
│  │     RDS     │      │ ElastiCache │                           │
│  │ PostgreSQL  │      │    Redis    │                           │
│  └─────────────┘      └─────────────┘                           │
│                                                                  │
│  ┌─────────────┐      ┌─────────────┐                           │
│  │     ECR     │      │  Secrets    │                           │
│  │  (Images)   │      │   Manager   │                           │
│  └─────────────┘      └─────────────┘                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## Step-by-Step Deployment

### Step 1: Deploy AWS Infrastructure

#### Option A: Using CloudFormation (Recommended)

```powershell
# Navigate to project directory
cd f:\dealharbor\DealHarbor-Backend

# Create the CloudFormation stack
aws cloudformation create-stack `
    --stack-name dealharbor-infrastructure `
    --template-body file://aws/cloudformation.yaml `
    --parameters ParameterKey=DBPassword,ParameterValue=YourSecurePassword123! `
    --capabilities CAPABILITY_IAM `
    --region ap-south-1

# Wait for stack creation (takes ~15-20 minutes)
aws cloudformation wait stack-create-complete `
    --stack-name dealharbor-infrastructure `
    --region ap-south-1

# Get stack outputs
aws cloudformation describe-stacks `
    --stack-name dealharbor-infrastructure `
    --query "Stacks[0].Outputs" `
    --region ap-south-1
```

#### Option B: Manual Setup via AWS Console

1. **Create VPC:**
   - Go to VPC Dashboard → Create VPC
   - Choose "VPC and more"
   - Name: `dealharbor-vpc`
   - IPv4 CIDR: `10.0.0.0/16`
   - 2 Availability Zones, 2 public + 2 private subnets

2. **Create RDS PostgreSQL:**
   - Go to RDS → Create Database
   - Engine: PostgreSQL 15
   - Template: Free tier (for testing) or Production
   - DB Instance: `dealharbor-db`
   - Master username: `postgres`
   - Master password: `<your-secure-password>`
   - Instance: `db.t3.micro`
   - VPC: Select `dealharbor-vpc`
   - Public access: No
   - Create new security group: `dealharbor-rds-sg`

3. **Create ElastiCache Redis:**
   - Go to ElastiCache → Create cluster
   - Cluster engine: Redis
   - Name: `dealharbor-redis`
   - Node type: `cache.t3.micro`
   - Number of replicas: 0 (for cost savings)
   - VPC: `dealharbor-vpc`
   - Subnet group: Create new with private subnets

4. **Create ECR Repository:**
   - Go to ECR → Create repository
   - Name: `dealharbor-backend-production`
   - Enable scan on push

5. **Create ECS Cluster:**
   - Go to ECS → Create cluster
   - Name: `dealharbor-cluster-production`
   - Infrastructure: AWS Fargate

---

### Step 2: Store Secrets in AWS Secrets Manager

```powershell
# Create database secrets
aws secretsmanager create-secret `
    --name dealharbor/database `
    --secret-string '{
        "DATABASE_URL": "jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/postgres",
        "DATABASE_USERNAME": "postgres",
        "DATABASE_PASSWORD": "YourSecurePassword123!"
    }' `
    --region ap-south-1

# Create Redis secrets
aws secretsmanager create-secret `
    --name dealharbor/redis `
    --secret-string '{
        "REDIS_HOST": "YOUR_ELASTICACHE_ENDPOINT"
    }' `
    --region ap-south-1

# Create application secrets
aws secretsmanager create-secret `
    --name dealharbor/app `
    --secret-string '{
        "JWT_SECRET": "your-super-secure-jwt-secret-key-at-least-256-bits-long"
    }' `
    --region ap-south-1

# Create mail secrets
aws secretsmanager create-secret `
    --name dealharbor/mail `
    --secret-string '{
        "MAIL_USERNAME": "your-email@gmail.com",
        "MAIL_PASSWORD": "your-app-password",
        "MAIL_FROM": "your-email@gmail.com"
    }' `
    --region ap-south-1

# Create OAuth secrets
aws secretsmanager create-secret `
    --name dealharbor/oauth `
    --secret-string '{
        "GOOGLE_CLIENT_ID": "your-google-client-id",
        "GOOGLE_CLIENT_SECRET": "your-google-client-secret",
        "GITHUB_CLIENT_ID": "your-github-client-id",
        "GITHUB_CLIENT_SECRET": "your-github-client-secret",
        "OAUTH2_REDIRECT_URI": "https://your-frontend.com/oauth2/redirect"
    }' `
    --region ap-south-1

# Create Supabase secrets
aws secretsmanager create-secret `
    --name dealharbor/supabase `
    --secret-string '{
        "SUPABASE_PROJECT_URL": "https://your-project.supabase.co",
        "SUPABASE_SERVICE_ROLE_KEY": "your-service-role-key",
        "SUPABASE_PUBLIC_URL": "https://your-project.supabase.co/storage/v1/object/public/product-images"
    }' `
    --region ap-south-1
```

---

### Step 3: Create IAM Roles

```powershell
# Create ECS Task Execution Role
aws iam create-role `
    --role-name ecsTaskExecutionRole `
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"Service": "ecs-tasks.amazonaws.com"},
            "Action": "sts:AssumeRole"
        }]
    }'

# Attach policies
aws iam attach-role-policy `
    --role-name ecsTaskExecutionRole `
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Create policy for Secrets Manager access
aws iam put-role-policy `
    --role-name ecsTaskExecutionRole `
    --policy-name SecretsManagerAccess `
    --policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue"
            ],
            "Resource": "arn:aws:secretsmanager:ap-south-1:*:secret:dealharbor/*"
        }]
    }'

# Create ECS Task Role (for application)
aws iam create-role `
    --role-name ecsTaskRole `
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"Service": "ecs-tasks.amazonaws.com"},
            "Action": "sts:AssumeRole"
        }]
    }'
```

---

### Step 4: Build and Push Docker Image

```powershell
# Navigate to project directory
cd f:\dealharbor\DealHarbor-Backend

# Set variables (replace with your values)
$AWS_ACCOUNT_ID = "YOUR_ACCOUNT_ID"
$AWS_REGION = "ap-south-1"
$ECR_REPO = "dealharbor-backend-production"

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# Build the Docker image
docker build -t ${ECR_REPO}:latest .

# Tag for ECR
docker tag ${ECR_REPO}:latest "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${ECR_REPO}:latest"

# Push to ECR
docker push "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${ECR_REPO}:latest"
```

---

### Step 5: Create ECS Task Definition

1. Edit `aws/task-definition.json`:
   - Replace `YOUR_ACCOUNT_ID` with your AWS Account ID
   - Replace `YOUR_REGION` with `ap-south-1`

2. Register the task definition:

```powershell
aws ecs register-task-definition `
    --cli-input-json file://aws/task-definition.json `
    --region ap-south-1
```

---

### Step 6: Create ECS Service

```powershell
# Get the subnet and security group IDs from CloudFormation outputs
$STACK_OUTPUTS = aws cloudformation describe-stacks `
    --stack-name dealharbor-infrastructure `
    --query "Stacks[0].Outputs" `
    --region ap-south-1 | ConvertFrom-Json

# Create the ECS service
aws ecs create-service `
    --cluster dealharbor-cluster-production `
    --service-name dealharbor-service `
    --task-definition dealharbor-backend `
    --desired-count 1 `
    --launch-type FARGATE `
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" `
    --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:ap-south-1:xxx:targetgroup/xxx,containerName=dealharbor-backend,containerPort=8080" `
    --region ap-south-1
```

Or use the AWS Console:
1. Go to ECS → Clusters → `dealharbor-cluster-production`
2. Click "Create service"
3. Configure:
   - Launch type: Fargate
   - Task definition: `dealharbor-backend`
   - Service name: `dealharbor-service`
   - Desired tasks: 1
   - VPC: `dealharbor-vpc`
   - Subnets: Select public subnets
   - Security group: Select ECS security group
   - Load balancer: Select ALB and target group

---

### Step 7: Configure Security Groups

Ensure security groups allow:

1. **ALB Security Group:**
   - Inbound: 80, 443 from 0.0.0.0/0

2. **ECS Security Group:**
   - Inbound: 8080 from ALB Security Group

3. **RDS Security Group:**
   - Inbound: 5432 from ECS Security Group

4. **Redis Security Group:**
   - Inbound: 6379 from ECS Security Group

---

### Step 8: Verify Deployment

```powershell
# Check service status
aws ecs describe-services `
    --cluster dealharbor-cluster-production `
    --services dealharbor-service `
    --region ap-south-1

# Get ALB DNS name
aws cloudformation describe-stacks `
    --stack-name dealharbor-infrastructure `
    --query "Stacks[0].Outputs[?OutputKey=='ALBDNSName'].OutputValue" `
    --output text `
    --region ap-south-1
```

Test the API:
```powershell
# Health check
curl http://YOUR_ALB_DNS/actuator/health
```

---

## CI/CD Setup

### GitHub Actions (Automatic Deployment)

1. Go to your GitHub repository → Settings → Secrets and variables → Actions

2. Add these secrets:
   - `AWS_ACCESS_KEY_ID`: Your AWS access key
   - `AWS_SECRET_ACCESS_KEY`: Your AWS secret key

3. Push to `main` branch to trigger deployment:

```powershell
git add .
git commit -m "Deploy to AWS"
git push origin main
```

---

## Cost Estimation (Monthly)

| Service | Configuration | Estimated Cost |
|---------|--------------|----------------|
| ECS Fargate | 0.5 vCPU, 1GB RAM | ~$15-20 |
| RDS PostgreSQL | db.t3.micro | ~$15-20 |
| ElastiCache Redis | cache.t3.micro | ~$12-15 |
| ALB | Standard | ~$20-25 |
| ECR | <1GB storage | ~$0.10 |
| Data Transfer | ~10GB | ~$1 |
| **Total** | | **~$65-80/month** |

### Cost Optimization Tips:
- Use RDS Reserved Instances for 30-60% savings
- Use Fargate Spot for non-critical workloads
- Consider using Aurora Serverless for variable workloads

---

## Troubleshooting

### Common Issues

1. **Task fails to start:**
   ```powershell
   # Check task logs
   aws logs get-log-events `
       --log-group-name /ecs/dealharbor-backend `
       --log-stream-name "ecs/dealharbor-backend/TASK_ID" `
       --region ap-south-1
   ```

2. **Database connection issues:**
   - Verify security group allows connection from ECS
   - Check DATABASE_URL format
   - Ensure RDS is in the same VPC

3. **Redis connection issues:**
   - Verify ElastiCache security group
   - Ensure Redis is in private subnet accessible by ECS

4. **Health check failing:**
   - Verify `/actuator/health` endpoint is accessible
   - Check application logs for startup errors
   - Ensure sufficient memory/CPU allocated

---

## Scaling

### Manual Scaling
```powershell
aws ecs update-service `
    --cluster dealharbor-cluster-production `
    --service dealharbor-service `
    --desired-count 3 `
    --region ap-south-1
```

### Auto Scaling (Optional)
Set up Application Auto Scaling based on CPU/Memory metrics.

---

## SSL/HTTPS Setup

1. Request SSL certificate from AWS Certificate Manager (ACM)
2. Add HTTPS listener to ALB on port 443
3. Redirect HTTP to HTTPS

```powershell
# Request certificate
aws acm request-certificate `
    --domain-name api.yourdomain.com `
    --validation-method DNS `
    --region ap-south-1
```

---

## Cleanup

To delete all resources:

```powershell
# Delete ECS service
aws ecs delete-service `
    --cluster dealharbor-cluster-production `
    --service dealharbor-service `
    --force `
    --region ap-south-1

# Delete CloudFormation stack
aws cloudformation delete-stack `
    --stack-name dealharbor-infrastructure `
    --region ap-south-1

# Delete secrets
aws secretsmanager delete-secret --secret-id dealharbor/database --region ap-south-1
aws secretsmanager delete-secret --secret-id dealharbor/redis --region ap-south-1
aws secretsmanager delete-secret --secret-id dealharbor/app --region ap-south-1
aws secretsmanager delete-secret --secret-id dealharbor/mail --region ap-south-1
aws secretsmanager delete-secret --secret-id dealharbor/oauth --region ap-south-1
aws secretsmanager delete-secret --secret-id dealharbor/supabase --region ap-south-1
```

---

## Support

For issues, check:
1. ECS task logs in CloudWatch
2. Application Load Balancer access logs
3. RDS/ElastiCache metrics in CloudWatch
