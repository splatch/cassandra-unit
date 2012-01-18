package org.cassandraunit.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.FileDataSet;

public class CassandraUnitCommandLineLoader {

	private static CommandLineParser commandLineParser = null;

	private static Options options = null;

	private static CommandLine commandLine = null;

	private static boolean usageBeenPrinted = false;

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) {
		boolean exit = parseCommandLine(args);
		if (exit) {
			System.exit(1);
		} else {
			load();
		}

	}

	protected static boolean parseCommandLine(String[] args) {
		clearStaticAttributes();
		initOptions();
		commandLineParser = new PosixParser();
		boolean exit = false;
		try {
			commandLine = commandLineParser.parse(options, args);
			if (commandLine.getOptions().length == 0) {
				exit = true;
				printUsage();
			} else {
				if (containBadReplicationFactorArgumentValue()) {
					printUsage("Bad argument value for option r");
					exit = true;
				}
			}
		} catch (ParseException e) {
			printUsage(e.getMessage());
			exit = true;
		}

		return exit;

	}

	protected static void load() {
		String host = commandLine.getOptionValue("h");
		String port = commandLine.getOptionValue("p");
		String file = commandLine.getOptionValue("f");
		String clusterName = commandLine.getOptionValue("c");
		boolean onlySchema = commandLine.hasOption("o");
		boolean overrideReplicationFactor = false;
		int replicationFactor = 0;
		if (commandLine.hasOption("r")) {
			overrideReplicationFactor = true;
			replicationFactor = Integer.parseInt(commandLine.getOptionValue("r"));
		}
		boolean overrideStrategy = false;
		if (commandLine.hasOption("s")) {
			overrideStrategy = true;
			String strategy = commandLine.getOptionValue("s");
		}

		DataLoader dataLoader = new DataLoader(clusterName, host + ":" + port);
		dataLoader.load(new FileDataSet(file));
	}

	private static boolean containBadReplicationFactorArgumentValue() {
		String replicationFactor = commandLine.getOptionValue("r");
		if (replicationFactor != null && !replicationFactor.trim().isEmpty()) {
			try {
				Integer.parseInt(replicationFactor);
				return false;
			} catch (NumberFormatException e) {
				return true;
			}
		}
		return false;

	}

	private static void printUsage(String message) {
		System.out.println(message);
		printUsage();

	}

	private static void initOptions() {
		options = new Options();
		options.addOption(OptionBuilder.withLongOpt("file").hasArg().withDescription("dataset to load").isRequired()
				.create("f"));
		options.addOption(OptionBuilder.withLongOpt("host").hasArg().withDescription("target host (required)")
				.isRequired().create("h"));
		options.addOption(OptionBuilder.withLongOpt("port").hasArg().withDescription("target port (required)")
				.isRequired().create("p"));
		options.addOption(OptionBuilder.withLongOpt("clusterName").hasArg().withDescription("cluster name")
				.isRequired().create("c"));
		options.addOption(OptionBuilder.withLongOpt("onlySchema").withDescription("only load schema (optional)")
				.create("o"));
		options.addOption(OptionBuilder.withLongOpt("replicationFactor").hasArg()
				.withDescription("override the replication factor set in the dataset (optional)").create("r"));
		options.addOption(OptionBuilder.withLongOpt("strategy").hasArg()
				.withDescription("override the strategy set in the dataset (optional)").create("s"));

	}

	private static void clearStaticAttributes() {
		commandLine = null;
		commandLineParser = null;
		options = null;
		usageBeenPrinted = false;
	}

	private static void printUsage() {
		usageBeenPrinted = true;
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("load", options);
	}

	protected static CommandLine getCommandLine() {
		return commandLine;
	}

	protected static boolean isUsageBeenPrinted() {
		return usageBeenPrinted;
	}
}
