import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

public class MatrixAddition {

	private final long[][] A, B, C;
	private final int threshold, noThreads, rows, cols;

	public MatrixAddition(long[][] A, long[][] B, int numThreads) {
		this.A = A;
		this.B = B;
		this.rows = A.length;
		this.cols = A[0].length;
		this.C = new long[rows][cols];
		this.noThreads = numThreads;
		this.threshold = Math.max(1, (rows * cols) / numThreads);
	}

	public long[][] nonParallelApproach() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				C[i][j] = A[i][j] + B[i][j];
			}
		}
		return C;
	}

	public long[][] forkJoinPoolApproach() {

		class ForkJoinTask extends RecursiveAction {
			private final int rowStart, rowEnd, colStart, colEnd;

			ForkJoinTask(int rowStart, int rowEnd, int colStart, int colEnd) {
				this.rowStart = rowStart;
				this.rowEnd = rowEnd;
				this.colStart = colStart;
				this.colEnd = colEnd;
			}

			@Override
			protected void compute() {
				int numRows = rowEnd - rowStart;
				int numCols = colEnd - colStart;

				if (numRows * numCols <= threshold) {
					for (int i = rowStart; i < rowEnd; i++) {
						for (int j = colStart; j < colEnd; j++) {
							C[i][j] = A[i][j] + B[i][j];
						}
					}
				} else {
					int midRow = rowStart + numRows / 2;
					int midCol = colStart + numCols / 2;

					invokeAll(
							new ForkJoinTask(rowStart, midRow, colStart, midCol),
							new ForkJoinTask(rowStart, midRow, midCol, colEnd),
							new ForkJoinTask(midRow, rowEnd, colStart, midCol),
							new ForkJoinTask(midRow, rowEnd, midCol, colEnd)
					);
				}
			}
		}

		ForkJoinPool pool = new ForkJoinPool();
		try {
			pool.invoke(new ForkJoinTask(0, rows, 0, cols));
			return C;
		} finally {
			pool.shutdown();
		}
	}

	public long[][] threadsApproach() throws InterruptedException {
		class ThreadTask implements Runnable {
			private final int rowStart, rowEnd, colStart, colEnd;

			ThreadTask(int rowStart, int rowEnd, int colStart, int colEnd) {
				this.rowStart = rowStart;
				this.rowEnd = rowEnd;
				this.colStart = colStart;
				this.colEnd = colEnd;
			}

			@Override
			public void run() {
				for (int i = rowStart; i < rowEnd; i++) {
					for (int j = colStart; j < colEnd; j++) {
						C[i][j] = A[i][j] + B[i][j];
					}
				}
			}
		}

		Thread[] threads = new Thread[noThreads];
		int chunkSize = (int) Math.ceil((double) rows / noThreads);

		for (int t = 0; t < noThreads; t++) {
			int rowStart = t * chunkSize;
			int rowEnd = Math.min(rows, rowStart + chunkSize);
			threads[t] = new Thread(new ThreadTask(rowStart, rowEnd, 0, cols));
			threads[t].start();
		}

		for (Thread thread : threads) {
			if (thread != null) thread.join();
		}
		return C;
	}


	public long[][] executorServiceApproach() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(noThreads);

		class ExecutorTask implements Runnable {
			private final int rowStart, rowEnd;

			ExecutorTask(int rowStart, int rowEnd) {
				this.rowStart = rowStart;
				this.rowEnd = rowEnd;
			}

			@Override
			public void run() {
				for (int i = rowStart; i < rowEnd; i++) {
					for (int j = 0; j < cols; j++) {
						C[i][j] = A[i][j] + B[i][j];
					}
				}
			}
		}

		Future<?>[] futures = new Future<?>[noThreads];
		int chunkSize = Math.max(1, rows / noThreads);

		for (int t = 0; t < noThreads; t++) {
			int rowStart = t * chunkSize;
			int rowEnd = Math.min(rows, rowStart + chunkSize);
			futures[t] = executor.submit(new ExecutorTask(rowStart, rowEnd));
		}

		for (Future<?> future : futures) {
			if (future != null) future.get();
		}

		executor.shutdown();
		return C;
	}

	public long[][] streamApproach() {
		java.util.stream.IntStream.range(0, rows).parallel().forEach(i -> {
			for (int j = 0; j < cols; j++) {
				C[i][j] = A[i][j] + B[i][j];
			}
		});
		return C;
	}
}
