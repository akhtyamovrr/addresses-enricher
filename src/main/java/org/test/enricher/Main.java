package org.test.enricher;

import org.test.enricher.datasource.TSVAddressesDataSource;
import org.test.enricher.executor.AddressesEnricherExecutor;
import org.test.enricher.writer.TSVAddressWriter;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("At least 1 argument required.\nArguments: <input_file_path> [output_file_path] " +
                    "[distance_to_search_meters] [max_distance_to_neighbours]\n" +
                    "distance_to_search_meters - area around where houses with same zip code and city are searched around\n" +
                    "max_distance_to_neighbours - maximum distance to closest house. If bigger, it may be street with " +
                    "the same name at other city\n" +
                    "If no output file is declared, result will be stored to 'result.tsv' at work directory");
            return;
        }

        final var inputFile = args[0];
        final var outputFile = args.length >= 2 ? args[1] : "result.tsv";
        final var metersToScanAround = args.length >= 3 ? Double.parseDouble(args[2]) : 50.0;
        final var maxDistanceToSameStreetNeighboursMeters = args.length >= 4 ? Double.parseDouble(args[2]) : 1000.0;
        final var dataSource = new TSVAddressesDataSource(inputFile);
        final var writer = new TSVAddressWriter(outputFile);
        final var start = System.currentTimeMillis();
        final var executor = new AddressesEnricherExecutor(dataSource, writer, metersToScanAround, maxDistanceToSameStreetNeighboursMeters);
        executor.execute();
        final var end = System.currentTimeMillis();
        System.out.println("Processing took: " + (end - start));
    }
}
