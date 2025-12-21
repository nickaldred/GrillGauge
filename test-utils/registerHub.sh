#!/bin/bash

REGISTER_HUB_URL="http://localhost:8080/api/v1/register/register"
MODEL="Hub-X1"
FW_VERSION="v1.0.4"

echo "Sending registration request to $REGISTER_HUB_URL"

response=$(curl -vv -s -X POST "$REGISTER_HUB_URL" \
     -H "Content-Type: application/json" \
     -d "{
           \"model\": \"$MODEL\",
           \"fwVersion\": \"$FW_VERSION\"
         }")

echo "--------------------------"
echo "Response from Server:"
echo "$response"
echo "--------------------------"