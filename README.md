# You need a fahrschein to use the (nakadi event) bus

## Example usage


    public class Main {
        private static final Logger LOG = LoggerFactory.getLogger(Main.class);

        public static class SalesOrderPlaced {
            private final SalesOrder salesOrder;

            @JsonCreator
            public SalesOrderPlaced(SalesOrder salesOrder) {
                this.salesOrder = salesOrder;
            }

            public SalesOrder getSalesOrder() {
                return salesOrder;
            }
        }


        public static class SalesOrder {
            private final String orderNumber;

            @JsonCreator
            public SalesOrder(String orderNumber) {
                this.orderNumber = orderNumber;
            }

            public String getOrderNumber() {
                return orderNumber;
            }
        }

        public static void main(String[] args) throws MalformedURLException {
            final URL url = new URL("https://nakadi.example.com/");
            final String eventName = "sales-order-service.order-placed";

            final ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerModule(new Jdk8Module());
            objectMapper.registerModule(new MoneyModule());
            objectMapper.registerModule(new ProblemModule());
            objectMapper.registerModule(new GuavaModule());
            objectMapper.registerModule(new ParameterNamesModule());

            final Listener<SalesOrderPlaced> listener = events -> {
                if (Math.random() < 0.01) {
                    // For testing reconnection logic
                    throw new EventProcessingException("Random failure");
                } else {
                    for (SalesOrderPlaced salesOrderPlaced : events) {
                        LOG.info("Received sales order [{}]", salesOrderPlaced.getSalesOrder().getOrderNumber());
                    }
                }
            };

            final InMemoryCursorManager cursorManager = new InMemoryCursorManager();
            final AccessTokenProvider tokenProvider = new ZignAccessTokenProvider();
            final ConnectionParameters connectionParameters = new ConnectionParameters();

            final NakadiClient nakadiClient = new NakadiClient(baseUrl, connectionParameters, tokenProvider, objectMapper);
            final List<Partition> partitions = nakadiClient.getPartitions(eventName);

            for (Partition partition : partitions) {
                LOG.info("Partition [{}] has oldest offset [{}] and newest offset [{}]", partition.getPartition(), partition.getOldestAvailableOffset(), partition.getNewestAvailableOffset());
            }

            cursorManager.fromOldestAvailableOffset(partitions);

            nakadiClient.listen(eventName, SalesOrderPlaced.class, listener, cursorManager);
        }
    }
