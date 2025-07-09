# Stage 1: Build the Go application
FROM golang:1.24-alpine AS builder

# Set the Current Working Directory inside the container
WORKDIR /app

# We want to populate the module cache based on the go.{mod,sum} files.
# Copy go.mod and go.sum files to the workspace
COPY go.mod go.sum ./

# Download all dependencies. Dependencies will be cached if the go.mod and go.sum files are not changed
RUN go mod download

# Copy the source code into the container
COPY . .

# Build the Go app
# CGO_ENABLED=0 builds a statically linked binary (no external C dependencies)
# -ldflags="-s -w" strips debugging information, reducing binary size
RUN CGO_ENABLED=0 GOOS=linux go build -a -ldflags="-s -w" -o /app/appointment-service .

# Stage 2: Create the final, minimal image
FROM alpine:latest

WORKDIR /app

# Copy the pre-built binary from the builder stage
COPY --from=builder /app/appointment-service .

# Expose port 8080 to the outside world
EXPOSE 8080

# Command to run the executable
CMD ["./appointment-service"]