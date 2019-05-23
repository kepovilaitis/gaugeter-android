package lt.kepo.gaugeter.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.holders.JobHolder;
import lt.kepo.gaugeter.tools.Utils;

import java.util.ArrayList;
import java.util.List;

public class CompletedJobFragment extends BaseFragment {

    private LineChart _chart;

    private JobHolder _job = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null){
            _job = (JobHolder) args.getSerializable(JobHolder.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_completed_job, container, false);

        _chart = main.findViewById(R.id.chart);

        setTitle(Utils.getFormattedDateTime(_job.getDateCreated()));

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(getOilPressureDataSet());
        dataSets.add(getOilTemperatureDataSet());
        dataSets.add(getWaterTemperatureDataSet());
        dataSets.add(getChargeDataSet());

        _chart.setData(new LineData(dataSets));
        _chart.setDescription(null);
        _chart.invalidate();

        setUpLeftYAxis();
        setUpXAxis();

        _chart.getAxisRight().setEnabled(false);

        return main;
    }

    private LineDataSet getOilTemperatureDataSet(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0.0f));

        for (int i = 0; i < _job.getTelemData().size(); i++) {
            entries.add(new Entry(i, _job.getTelemData().get(i).getOilTemperature()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Oil Temperature");
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(getResources().getColor(R.color.chart_blue));
        dataSet.setLineWidth(1.0f);
        dataSet.setAxisDependency(AxisDependency.LEFT);
        dataSet.setValueTextColor(getResources().getColor(R.color.chart_blue));

        return dataSet;
    }

    private LineDataSet getOilPressureDataSet(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0.0f));

        for (int i = 0; i < _job.getTelemData().size(); i++) {
            entries.add(new Entry(i, _job.getTelemData().get(i).getOilPressure() - 30));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Oil Pressure");
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(getResources().getColor(R.color.chart_green));
        dataSet.setLineWidth(1.0f);
        dataSet.setAxisDependency(AxisDependency.LEFT);
        dataSet.setValueTextColor(getResources().getColor(R.color.chart_green));

        return dataSet;
    }

    private LineDataSet getWaterTemperatureDataSet(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0.0f));

        for (int i = 0; i < _job.getTelemData().size(); i++) {
            entries.add(new Entry(i, _job.getTelemData().get(i).getWaterTemperature() - 20));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Water Temperature");
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(getResources().getColor(R.color.chart_red));
        dataSet.setLineWidth(1.0f);
        dataSet.setAxisDependency(AxisDependency.RIGHT);
        dataSet.setValueTextColor(getResources().getColor(R.color.chart_red));

        return dataSet;
    }

    private LineDataSet getChargeDataSet(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0.0f));

        for (int i = 0; i < _job.getTelemData().size(); i++) {
            entries.add(new Entry(i, _job.getTelemData().get(i).getCharge() - 10));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Charge");
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(getResources().getColor(R.color.chart_yellow));
        dataSet.setLineWidth(1.0f);
        dataSet.setAxisDependency(AxisDependency.RIGHT);
        dataSet.setValueTextColor(getResources().getColor(R.color.chart_yellow));

        return dataSet;
    }

    private void setUpXAxis() {
        XAxis axisX = _chart.getXAxis();
        axisX.setPosition(XAxisPosition.BOTTOM);
        axisX.setDrawAxisLine(false);
        axisX.setDrawGridLines(true);
        axisX.setTextColor(ContextCompat.getColor(_context, android.R.color.black));
        axisX.setGridColor(ContextCompat.getColor(_context, android.R.color.black));
        axisX.setCenterAxisLabels(true);
        axisX.setGridLineWidth(0.75f);
    }

    private void setUpLeftYAxis() {
        YAxis axis = _chart.getAxisLeft();
        axis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
        axis.setDrawGridLines(true);
        axis.setGranularityEnabled(true);
        axis.setGridLineWidth(0.75f);
        axis.setTextColor(ContextCompat.getColor(_context, android.R.color.black));
        axis.setGridColor(ContextCompat.getColor(_context, android.R.color.black));
        axis.setDrawLimitLinesBehindData(true);
    }
}
