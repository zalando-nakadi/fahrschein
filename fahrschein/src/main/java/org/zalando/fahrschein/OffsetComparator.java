package org.zalando.fahrschein;

import java.util.Comparator;

class OffsetComparator implements Comparator<String> {
    static final Comparator<String> INSTANCE = new OffsetComparator();

    private OffsetComparator() {

    }

    @Override
    public int compare(String o1, String o2) {
        if ("BEGIN".equals(o1)) {
            return "BEGIN".equals(o2) ? 0 : -1;
        } else {
            int l1 = o1.length();
            int l2 = o2.length();
            if (l1 < l2) {
                return -1;
            } else if (l1 > l2) {
                return 1;
            } else {
                return o1.compareTo(o2);
            }
        }
    }
}
