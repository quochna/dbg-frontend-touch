package dbg.frontend.touch;

import com.vng.jcore.stats.EstimatedHistogram;
import com.vng.jcore.stats.LatencyTracker;

import java.text.SimpleDateFormat;
import java.util.Date;

import  java.text.DecimalFormat;
import java.text.NumberFormat;

public class Monitor
{
	
    private long startedStatsTime = 0;
    private final LatencyTracker latencyTracker = new LatencyTracker();
    private final EstimatedHistogram totalHistogram = new EstimatedHistogram();

    public Monitor()
    {
        this.startedStatsTime =  System.currentTimeMillis();
    }

    public synchronized void addMicro(long micros)
    {
        this.latencyTracker.addMicro(micros);
        this.totalHistogram.add(micros);
    }

    public void resetStat()
    {
        this.totalHistogram.clear();
        this.latencyTracker.clear();
        this.startedStatsTime = System.currentTimeMillis();
    }

    public long getStartedStatTime()
    {
        return this.startedStatsTime;
    }

    public long getTotalOpsPerSec()
    {
        return this.latencyTracker.getTotalOpsPerSec();
    }

    public long getOpCount()
    {
        return this.latencyTracker.getOpCount();
    }

    public long getLatencyPeak()
    {
        return this.latencyTracker.getLatencyPeak();
    }

    public long getLatencyPeakTime()
    {
        return this.latencyTracker.getLatencyPeakTime();
    }

    public long getRPSPeak()
    {
        return this.latencyTracker.getRPSPeak();
    }

    public long getRPSPeakTime()
    {
        return this.latencyTracker.getRPSPeakTime();
    }

    public long getTotalLatencyMicros()
    {
        return this.latencyTracker.getTotalLatencyMicros();
    }

    public long[] getTotalLatencyHistogramMicros()
    {
        return this.totalHistogram.get(false);
    }

    public long[] getBucketOffset()
    {
        return this.totalHistogram.getBucketOffset().clone();
    }

    public double getRecentLatencyMicros() {
        return this.latencyTracker.getRecentLatencyMicros();
    }

    public double getMeanLatency()
    {
        return this.latencyTracker.getAvgLatency();
    }

    public String dumpRawStats()
    {
        NumberFormat formatter = new DecimalFormat("#0.00000");
        String content = "";

        content = "req/s:" +  this.getTotalOpsPerSec() + "\n"				
                        + "peak_req/s:" + this.getRPSPeak() + "\n"
                        + "avg_latencty:" + formatter.format(this.latencyTracker.getAvgLatency()) + "\n"
                        + "peak_latency:" + (this.getLatencyPeak() / 1000) + "\n";

        return content;
    }

    public String dumpHtmlStats()
    {
        String content = "<html>"
                        + "<head>"
                        + "</head>"
                        + "<body>";
        SimpleDateFormat dateformat = new SimpleDateFormat();
        NumberFormat formatter = new DecimalFormat("#0.000");


        content = "<ul>"
                        + "<li>started Stats Time : " + dateformat.format(new Date(this.getStartedStatTime())) + "</li>"
                        + "<li>total req accumulate : " + this.getOpCount() + "</li>"
                        + "<li>total time elapsed : " + (System.currentTimeMillis() - this.startedStatsTime) + " msecs</li>"
                        + "<li>req/s : " + this.getTotalOpsPerSec() + "</li>"
                        + "<li>peak req/s : " + this.getRPSPeak() + " - " + dateformat.format(new Date(this.getRPSPeakTime())) + "</li>"
                        + "<li>avg latency : " + formatter.format(this.latencyTracker.getAvgLatency()) + " msecs </li>"
                        + "<li>peak latency : " + (this.getLatencyPeak() / 1000) + " ms - " + dateformat.format(new Date(this.getLatencyPeakTime())) + "</li>"
                        + "</ul>";

        long[] history = this.getTotalLatencyHistogramMicros();
        long[] bucketoffset = this.getBucketOffset();

        long total = 0;

        if(history != null && history.length > 0) {

                for(int i=0;i<history.length;i++) {
                        total += history[i];
                }

                if(total == 0) total = 1;

                double ratio = 0;

                long step_total = 0;

                content += "<ul>";
                for(int i=0;i<history.length;i++) {
                        step_total += history[i];
                        ratio = step_total * 100;
                        ratio = (ratio) / total ;
                        if(i < bucketoffset.length) content += "<li>< " + formatter.format((double)bucketoffset[i] / 1000) + " msec : \t" + history[i] + "\t" + displayProgressBar(ratio) + "</li>";
                        else content += "<li>" + "> " + bucketoffset[bucketoffset.length - 1] + ":" + history[i] + "</li>";
                }
                content += "</ul>";
        }

        content += "</body>"
                        + "</html>";

        return content;
    }

    private String displayProgressBar(double percent)
    {
        NumberFormat formatter = new DecimalFormat("#0.00");

        String content = "";

        content = "<div style=\"width:700px;padding-bottom:5px\"><div id=\"progress\" style=\"border: 1px solid black; width:500px;display:inline-block\">"
                        + "<div style=\"background-color:green; width:" + formatter.format(percent) + "%\">&nbsp;</div>"
                                        + "</div> " + formatter.format(percent) + " %</div>";
        return content;
    }

}
