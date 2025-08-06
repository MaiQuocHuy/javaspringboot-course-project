# ================================
# Docker Build & Push Script for KTC Spring Boot Application
# ================================

param(
    [Parameter(Mandatory=$false)]
    [string]$DockerUsername = "",
    
    [Parameter(Mandatory=$false)]
    [string]$ImageName = "ktc-learning-platform",
    
    [Parameter(Mandatory=$false)]
    [string]$Tag = "latest",
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipPush = $false
)

# Colors for output
$Red = [System.ConsoleColor]::Red
$Green = [System.ConsoleColor]::Green
$Yellow = [System.ConsoleColor]::Yellow
$Blue = [System.ConsoleColor]::Blue

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    } else {
        $input | Write-Output
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Step($Message) {
    Write-ColorOutput $Blue "🚀 $Message"
}

function Write-Success($Message) {
    Write-ColorOutput $Green "✅ $Message"
}

function Write-Error($Message) {
    Write-ColorOutput $Red "❌ $Message"
}

function Write-Warning($Message) {
    Write-ColorOutput $Yellow "⚠️  $Message"
}

# Main execution
try {
    Write-Step "Starting Docker build process for KTC Learning Platform..."
    
    # Kiểm tra Docker có được cài đặt không
    Write-Step "Checking Docker installation..."
    $dockerVersion = docker --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker is not installed or not in PATH!"
        exit 1
    }
    Write-Success "Docker found: $dockerVersion"
    
    # Kiểm tra Docker daemon có chạy không
    Write-Step "Checking Docker daemon..."
    docker info 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker daemon is not running! Please start Docker Desktop."
        exit 1
    }
    Write-Success "Docker daemon is running"
    
    if (-not $SkipBuild) {
        # Build JAR file trước
        Write-Step "Building Spring Boot JAR file..."
        .\mvnw.cmd clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to build JAR file!"
            exit 1
        }
        Write-Success "JAR file built successfully"
    }
    
    # Build Docker image
    Write-Step "Building Docker image..."
    $fullImageName = if ($DockerUsername) { "$DockerUsername/$ImageName`:$Tag" } else { "$ImageName`:$Tag" }
    
    Write-ColorOutput $Yellow "Building image: $fullImageName"
    docker build -t $fullImageName .
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build Docker image!"
        exit 1
    }
    Write-Success "Docker image built successfully: $fullImageName"
    
    # Hiển thị thông tin image
    Write-Step "Docker image information:"
    docker images $fullImageName
    
    # Push to Docker Hub (nếu có username)
    if (-not $SkipPush -and $DockerUsername) {
        Write-Step "Pushing image to Docker Hub..."
        Write-ColorOutput $Yellow "Pushing: $fullImageName"
        
        # Kiểm tra đã đăng nhập Docker Hub chưa
        $loginCheck = docker info 2>$null | Select-String "Username"
        if (-not $loginCheck) {
            Write-Warning "You need to login to Docker Hub first!"
            Write-ColorOutput $Yellow "Run: docker login"
            exit 1
        }
        
        docker push $fullImageName
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to push Docker image!"
            exit 1
        }
        Write-Success "Docker image pushed successfully to Docker Hub!"
        Write-ColorOutput $Green "Image URL: https://hub.docker.com/r/$DockerUsername/$ImageName"
    } elseif (-not $DockerUsername) {
        Write-Warning "No Docker username provided. Skipping push to Docker Hub."
        Write-ColorOutput $Yellow "To push later, run: docker push $fullImageName"
    }
    
    Write-Success "🎉 Docker build process completed successfully!"
    Write-ColorOutput $Blue "📋 Summary:"
    Write-ColorOutput $Blue "   - Image Name: $fullImageName"
    Write-ColorOutput $Blue "   - Image Size: $(docker images $fullImageName --format 'table {{.Size}}')"
    Write-ColorOutput $Blue "   - Created: $(Get-Date)"
    
    Write-ColorOutput $Yellow "🚀 To run the container:"
    Write-ColorOutput $Yellow "   docker run -p 8080:8080 -e DB_URL='jdbc:mysql://host.docker.internal:3306/course_management' -e DB_USERNAME='your_username' -e DB_PASSWORD='your_password' $fullImageName"
    
} catch {
    Write-Error "An error occurred: $_"
    exit 1
}
