package org.test.enricher;

import org.test.enricher.datasource.TSVAddressesDataSource;
import org.test.enricher.executor.AddressesEnricherExecutor;
import org.test.enricher.writer.TSVAddressWriter;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("At least 1 argument required.\nArguments: <input_file_path> [output_file_path] [distance_to_search_meters]\n" +
                    "If no output file is declared, result will be stored to 'result.tsv' at work directory");
            return;
        }

        final var inputFile = args[0];
        final var outputFile = args.length >= 2 ? args[1] : "result.tsv";
        final var metersToScanAround = args.length >= 3 ? Double.parseDouble(args[2]) : 50.0;
        final var dataSource = new TSVAddressesDataSource(inputFile);
        final var writer = new TSVAddressWriter(outputFile);
        final var start = System.currentTimeMillis();
        final var executor = new AddressesEnricherExecutor(dataSource, writer, metersToScanAround);
        executor.execute();
        final var end = System.currentTimeMillis();
        System.out.println("Processing took: " + (end - start));
    }
}
