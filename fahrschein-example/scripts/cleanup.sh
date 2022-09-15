#!/bin/bash

for i in $(curl -s localhost:8080/subscriptions | jq  -r '.items[] .id'); do
  curl -s -XDELETE localhost:8080/subscriptions/"$i"
done

curl -XDELETE localhost:8080/event-types/order_created
curl -XDELETE localhost:8080/event-types/payment_accepted
