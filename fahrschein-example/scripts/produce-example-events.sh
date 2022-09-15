#!/bin/bash

while true; do

  EPOCH_SEC=$(date '+%s')
  DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  UUID=$(uuidgen)

  curl -s -H "Content-Type: application/json" localhost:8080/event-types/order_created/events -d '[
  {
    "orderNumber": "'$EPOCH_SEC'",
    "customerNumber": "'$EPOCH_SEC'",
    "paymentMethod": "paypal",
    "grandTotal": {
      "currency": "EUR",
      "amount": 12.23
    },
    "metadata": {
      "eid": "'$UUID'",
      "occurred_at": "'$DATE'"
    }
  }]'

  sleep 1
  EPOCH_SEC=$(date '+%s')
  DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  UUID=$(uuidgen)

  curl -s -H "Content-Type: application/json" localhost:8080/event-types/payment_accepted/events -d '[
  {
    "orderNumber": "'$EPOCH_SEC'",
    "paymentMethod": "paypal",
    "metadata": {
      "eid": "'$UUID'",
      "occurred_at": "'$DATE'"
    }
  }]'
  sleep 1
done
