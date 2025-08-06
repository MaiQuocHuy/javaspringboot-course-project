# ================================

# Instructions for Docker Hub Login & Push

# ================================

# Step 1: Create Docker Hub Account (if you don't have one)

# Go to: https://hub.docker.com/signup

# Step 2: Login to Docker Hub from terminal

docker login

# You will be prompted for:

# Username: your-docker-hub-username

# Password: your-docker-hub-password

# Step 3: Tag your image with your Docker Hub username

docker tag ktc-learning-platform:latest YOUR_USERNAME/ktc-learning-platform:latest

# Step 4: Push to Docker Hub

docker push YOUR_USERNAME/ktc-learning-platform:latest

# Example with username "maiquochuy":

# docker tag ktc-learning-platform:latest maiquochuy/ktc-learning-platform:latest

# docker push maiquochuy/ktc-learning-platform:latest

# Optional: Push with specific version tag

# docker tag ktc-learning-platform:latest maiquochuy/ktc-learning-platform:v1.0.0

# docker push maiquochuy/ktc-learning-platform:v1.0.0
