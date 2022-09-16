#!/bin/bash

curl -H "Content-Type: application/json" localhost:8080/event-types -d '{
  "name": "order_created",
  "owning_application": "acme-order-service",
  "category": "business",
  "partition_strategy": "hash",
  "partition_key_fields": ["orderNumber"],
  "enrichment_strategies": ["metadata_enrichment"],
  "default_statistic": {
    "messages_per_minute": 1,
    "message_size": 5,
    "read_parallelism": 1,
    "write_parallelism": 1
  },
  "schema": {
    "type": "json_schema",
    "schema": "{ \"type\":\"object\", \"properties\":{ \"orderNumber\":{ \"type\":\"string\" }, \"customerNumber\":{ \"type\":\"string\" }, \"appDomainId\":{ \"type\":\"string\" }, \"grandTotal\":{ \"type\":\"object\", \"properties\": { \"amount\":{ \"format\":\"decimal\", \"type\":\"number\", \"example\":99.95 }, \"currency\":{ \"format\":\"iso-4217\", \"type\":\"string\", \"example\":\"EUR\" } } }, \"paymentMethod\":{ \"type\":\"string\" }, \"couponCode\":{ \"type\":\"string\" }, \"itemCount\":{ \"type\":\"string\" }, \"partnerIds\":{ \"type\":\"string\" }, \"shippingCountry\":{ \"type\":\"string\" }, \"flow_id\":{ \"type\":\"string\" }, \"li_environment\":{ \"type\":\"string\" }, \"li_host\":{ \"type\":\"string\" }, \"li_instance\":{ \"type\":\"string\" }, \"li_process_enabled\":{ \"type\":\"string\" }, \"li_application_name\":{ \"type\":\"string\" }, \"li_application_version\":{ \"type\":\"string\" } }, \"additionalProperties\":true }"
  }
}'

curl -H "Content-Type: application/json" localhost:8080/event-types -d '{
  "name": "payment_accepted",
  "owning_application": "acme-order-service",
  "category": "business",
  "partition_strategy": "hash",
  "partition_key_fields": ["orderNumber"],
  "enrichment_strategies": ["metadata_enrichment"],
  "default_statistic": {
    "messages_per_minute": 1,
    "message_size": 5,
    "read_parallelism": 1,
    "write_parallelism": 1
  },
  "schema": {
    "type": "json_schema",
    "schema": "{ \"type\":\"object\", \"properties\":{ \"orderNumber\":{ \"type\":\"string\" }, \"customerNumber\":{ \"type\":\"string\" }, \"appDomainId\":{ \"type\":\"string\" }, \"grandTotal\":{ \"type\":\"string\" }, \"paymentMethod\":{ \"type\":\"string\" }, \"couponCode\":{ \"type\":\"string\" }, \"itemCount\":{ \"type\":\"string\" }, \"partnerIds\":{ \"type\":\"string\" }, \"shippingCountry\":{ \"type\":\"string\" }, \"flow_id\":{ \"type\":\"string\" }, \"li_environment\":{ \"type\":\"string\" }, \"li_host\":{ \"type\":\"string\" }, \"li_instance\":{ \"type\":\"string\" }, \"li_process_enabled\":{ \"type\":\"string\" }, \"li_application_name\":{ \"type\":\"string\" }, \"li_application_version\":{ \"type\":\"string\" } }, \"additionalProperties\":true }"
  }
}'
