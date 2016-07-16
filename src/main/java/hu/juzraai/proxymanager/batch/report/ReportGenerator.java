package hu.juzraai.proxymanager.batch.report;

import org.easybatch.core.job.Job;
import org.easybatch.core.job.JobReport;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Utility for generating job report string from workers {@link JobReport}
 * objects.
 *
 * @author Zsolt Jurányi
 */
public class ReportGenerator {

	/**
	 * Merges workers' {@link JobReport} objects and generates a string with
	 * these values: total record count, skipped record count, filtered record
	 * count, failed record count, succeced record count, average processing
	 * time.
	 *
	 * @param reports {@link JobReport} objects returned by worker {@link Job}s
	 * @return Merged and readable report as string
	 */
	@Nonnull
	public String generateReport(@Nonnull List<JobReport> reports) {

		long total = 0;
		long skipped = 0;
		long filtered = 0;
		long error = 0;
		long success = 0;
		long duration = 0;

		for (JobReport report : reports) {
			total += report.getMetrics().getTotalCount() - 1; // -1 PoisonRecord
			skipped += report.getMetrics().getSkippedCount();
			filtered += report.getMetrics().getFilteredCount() - 1; // -1 PoisonRecord
			error += report.getMetrics().getErrorCount();
			success += report.getMetrics().getSuccessCount();
			duration += report.getMetrics().getDuration();
		}

		StringBuilder s = new StringBuilder();
		s.append(total).append(" total ");
		s.append(skipped).append(" skipped ");
		s.append(filtered).append(" filtered ");
		s.append(error).append(" failed ");
		s.append(success).append(" succeded; avg process time: ");
		s.append(String.format("%.3f", ((double) duration) / total)).append(" ms");

		return s.toString();
	}

}
