package com.polopoly.ps.ci.tool;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.ParseException;
import com.polopoly.ps.pcmd.parser.Parser;
import com.polopoly.util.client.PolopolyContext;

public class ProcessParameters implements Parameters {
    public class OperationParser implements Parser<ProcessOperation> {

        @Override
        public ProcessOperation parse(String string) throws ParseException {
            try {
                return ProcessOperation.valueOf(string.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ParseException(this, string, "Not a known operation. Valid values: " + getHelp());
            }
        }

        @Override
        public String getHelp() {
            StringBuffer result = new StringBuffer(100);

            for (ProcessOperation value : allowedOperations) {
                if (result.length() > 0) {
                    result.append(", ");
                }

                result.append(value.name().toLowerCase());
            }

            return "One of the following operations: " + result;
        }

    }

    private ProcessOperation operation;
    private ProcessOperation[] allowedOperations;

    ProcessParameters(ProcessOperation... allowedOperations) {
        this.allowedOperations = allowedOperations;
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
        operation = args.getArgument(0, new OperationParser());
    }

    public ProcessOperation getOperation() {
        return operation;
    }

    @Override
    public void getHelp(ParameterHelp help) {
        help.setArguments(new OperationParser(), "The operation to perform.");
    }

}
