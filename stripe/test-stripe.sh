#!/bin/bash

# Stripe Integration Test Script
# This script helps you quickly test the Stripe integration

echo "🚀 Starting Stripe Integration Test Environment"
echo "================================================"
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing npm dependencies..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ npm install failed"
        exit 1
    fi
    echo "✅ Dependencies installed"
    echo ""
fi

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ .env file not found. Please create one with your Stripe API keys."
    exit 1
fi

echo "🎯 Configuration:"
echo "   - Frontend: http://localhost:3000"
echo "   - Backend:  http://localhost:4242"
echo "   - Using test mode Stripe keys"
echo ""

echo "📋 Test Card Numbers:"
echo "   Success:   4242 4242 4242 4242"
echo "   3D Secure: 4000 0025 0000 3155"
echo "   Declined:  4000 0000 0000 9995"
echo ""

echo "🏃 Starting servers..."
echo "   (Press Ctrl+C to stop)"
echo ""

# Start the application
npm run dev
