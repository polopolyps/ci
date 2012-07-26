package com.polopoly.ps.ci;

public interface OutputFilter {

    OutputFilter NO_DEBUG = new OutputFilter() {
        @Override
        public boolean shouldBePrinted(String line) {
            return !line.contains("DEBUG");
        }
    };

    OutputFilter ALL = new OutputFilter() {
        @Override
        public boolean shouldBePrinted(String line) {
            return true;
        }
    };

    boolean shouldBePrinted(String line);

}
