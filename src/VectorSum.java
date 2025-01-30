import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class VectorSum {
	private final long[] array;
	private final int noThreads;
	private final int threshold;

	public VectorSum(long[] array, int noThreads) {
		this.array = array;
		this.noThreads = noThreads;
		this.threshold = (int) Math.ceil((double) this.array.length / this.noThreads);
	}

	public long nonParallelApproach() {
		long totalSum = 0;
		for (long element : array) {
			totalSum += element;
		}
		return totalSum;
	}

	public long threadsApproach() throws InterruptedException {

		class ThreadOperation extends Thread {
			private final int start;
			private final int end;
			private long result = 0;

			public ThreadOperation(int start, int end) {
				this.start = start;
				this.end = end;
			}

			@Override
			public void run() {
				for (int i = start; i < end; i++) {
					result += array[i];
				}
			}

			public long getResult() {
				return result;
			}
		}

		int chunkSize = (array.length + noThreads - 1) / noThreads;
		ThreadOperation[] threads = new ThreadOperation[noThreads];

		for (int i = 0; i < noThreads; i++) {
			int start = i * chunkSize;
			int end = Math.min(start + chunkSize, array.length);
			if (start < array.length) {
				threads[i] = new ThreadOperation(start, end);
				threads[i].start();
			}
		}

		long totalSum = 0;
		for (ThreadOperation thread : threads) {
			if (thread != null) {
				thread.join();
				totalSum += thread.getResult();
			}
		}

		return totalSum;
	}

	public long forkJoinPoolApproach() {

		class ForkJoinPoolOperation extends RecursiveTask<Long> {
			private final int start, end;

			public ForkJoinPoolOperation(int start, int end) {
				this.start = start;
				this.end = end;
			}

			@Override
			protected Long compute() {
				if (end - start <= threshold) {
					long sum = 0;
					for (int i = start; i < end; i++) {
						sum += array[i];
					}
					return sum;
				} else {
					int mid = (start + end) / 2;
					ForkJoinPoolOperation leftTask = new ForkJoinPoolOperation(start, mid);
					ForkJoinPoolOperation rightTask = new ForkJoinPoolOperation(mid, end);

					leftTask.fork();
					long rightResult = rightTask.compute();
					long leftResult = leftTask.join();

					return leftResult + rightResult;
				}
			}
		}

		ForkJoinPool pool = new ForkJoinPool();
		try {
			ForkJoinPoolOperation task = new ForkJoinPoolOperation(0, array.length);
			return pool.invoke(task);
		} finally {
			pool.shutdown();
		}
	}

	public long executorServiceApproach() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(noThreads);
		int chunkSize = (array.length + noThreads - 1) / noThreads;
		List<Callable<Long>> tasks = new ArrayList<>();

		for (int i = 0; i < noThreads; i++) {
			final int start = i * chunkSize;
			final int end = Math.min(start + chunkSize, array.length);

			tasks.add(() -> {
				long sum = 0;
				for (int j = start; j < end; j++) {
					sum += array[j];
				}
				return sum;
			});
		}

		long totalSum = 0;
		try {
			List<Future<Long>> results = executor.invokeAll(tasks);
			for (Future<Long> result : results) {
				totalSum += result.get();
			}
		} finally {
			executor.shutdown();
		}

		return totalSum;
	}

	public long streamApproach() {
		return IntStream.range(0, array.length / threshold + 1).parallel().mapToLong(i -> {
			int start = i * threshold;
			int end = Math.min(start + threshold, array.length);
			long sum = 0;
			for (int j = start; j < end; j++) {
				sum += array[j];
			}
			return sum;
		}).sum();
	}
}
