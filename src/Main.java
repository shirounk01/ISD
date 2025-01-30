import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class Main {

	final static int NO_TESTS = 3;
	final static int NO_THREADS = 1000;
	final static int NO_RUNS_PER_TEST = 5;
	final static String[] METHOD_STRINGS = new String[] { "threadsApproach", "forkJoinPoolApproach", "executorServiceApproach", "streamApproach" };
	final static Random RANDOM = new Random();
	final static long UPPER_BOUND_VALUE = 2;

	public static void main(String[] args) throws Exception {
		vectorSum();

		matrixAddition();

		matrixMultiplication();
	}

	private static void matrixMultiplication()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		int matrixSize = 10;

		System.out.format("\n%50s\n", "--- Matrix Multiplication ---");
		for (int i = 0; i < NO_TESTS; i++, matrixSize *= 10) {

			final int size = matrixSize;
			long[][] matrixA = java.util.stream.IntStream.range(0, size).mapToObj(row -> java.util.stream.IntStream
					.range(0, size).mapToLong(col -> RANDOM.nextLong(UPPER_BOUND_VALUE)).toArray()).toArray(long[][]::new);
			long[][] matrixB = java.util.stream.IntStream.range(0, size).mapToObj(row -> java.util.stream.IntStream
					.range(0, size).mapToLong(col -> RANDOM.nextLong(UPPER_BOUND_VALUE)).toArray()).toArray(long[][]::new);
			MatrixMultiplication mAOp = new MatrixMultiplication(matrixA, matrixB, NO_THREADS);
			System.out.println("\n>>> For two square matrices of size " + matrixSize + " (avg. time for "
					+ NO_RUNS_PER_TEST + " tests and " + NO_THREADS + " threads)");

			for (String methodString : METHOD_STRINGS) {

				long totalTime = 0;
				Method method = MatrixMultiplication.class.getMethod(methodString);
				for (int j = 0; j < NO_RUNS_PER_TEST; j++) {

					long startTime = System.nanoTime();
					method.invoke(mAOp);
					long endTime = System.nanoTime();

					totalTime += endTime - startTime;
				}
				long avgTime = totalTime / NO_RUNS_PER_TEST;
				System.out.format("%-25s: %dns (%fms)\n", method.getName(), avgTime, avgTime / 1e6);
			}
		}
	}

	private static void matrixAddition()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		int matrixSize = 10;

		System.out.format("\n%50s\n", "--- Matrix Addition ---");
		for (int i = 0; i < NO_TESTS; i++, matrixSize *= 10) {

			final int size = matrixSize;
			long[][] matrixA = java.util.stream.IntStream.range(0, size).mapToObj(row -> java.util.stream.IntStream
					.range(0, size).mapToLong(col -> RANDOM.nextLong(UPPER_BOUND_VALUE)).toArray()).toArray(long[][]::new);
			long[][] matrixB = java.util.stream.IntStream.range(0, size).mapToObj(row -> java.util.stream.IntStream
					.range(0, size).mapToLong(col -> RANDOM.nextLong(UPPER_BOUND_VALUE)).toArray()).toArray(long[][]::new);
			MatrixAddition mAOp = new MatrixAddition(matrixA, matrixB, NO_THREADS);
			System.out.println("\n>>> For two square matrices of size " + matrixSize + " (avg. time for "
					+ NO_RUNS_PER_TEST + " tests and " + NO_THREADS + " threads)");

			for (String methodString : METHOD_STRINGS) {

				long totalTime = 0;
				Method method = MatrixAddition.class.getMethod(methodString);
				for (int j = 0; j < NO_RUNS_PER_TEST; j++) {

					long startTime = System.nanoTime();
					method.invoke(mAOp);
					long endTime = System.nanoTime();

					totalTime += endTime - startTime;
				}
				long avgTime = totalTime / NO_RUNS_PER_TEST;
				System.out.format("%-25s: %dns (%fms)\n", method.getName(), avgTime, avgTime / 1e6);
			}
		}
	}

	private static void vectorSum()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		int arraySize = 10;

		System.out.format("\n%50s\n", "--- Vector Sum ---");
		for (int i = 0; i < NO_TESTS; i++, arraySize *= 10) {

			long[] array = new Random().longs(arraySize, 0, UPPER_BOUND_VALUE).toArray();
			VectorSum aOp = new VectorSum(array, NO_THREADS);
			System.out.println("\n>>> For an array of length " + arraySize + " (avg. time for " + NO_RUNS_PER_TEST
					+ " tests and " + NO_THREADS + " threads)");

			for (String methodString : METHOD_STRINGS) {

				long totalTime = 0;
				Method method = VectorSum.class.getMethod(methodString);
				for (int j = 0; j < NO_RUNS_PER_TEST; j++) {

					long startTime = System.nanoTime();
					method.invoke(aOp);
					long endTime = System.nanoTime();

					totalTime += endTime - startTime;
				}
				long avgTime = totalTime / NO_RUNS_PER_TEST;
				System.out.format("%-25s: %dns (%fms)\n", method.getName(), avgTime, avgTime / 1e6);
			}
		}
	}

}
