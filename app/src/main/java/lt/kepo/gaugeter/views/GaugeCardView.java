package lt.kepo.gaugeter.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.helpers.AnimationHelper;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@EViewGroup(R.layout.gauge_card)
public class GaugeCardView extends LinearLayout {

    @ViewById(R.id.gaugeView) GaugeView _gaugeView;
    @ViewById(R.id.gaugeText) TextView _gaugeText;
    @ViewById(R.id.value) TextView _value;
    @ViewById(R.id.units) TextView _units;
    @ViewById(R.id.gaugeChart) LineChart _chart;

    private LineData _lineData;
    private Context _context;

    private boolean _isChartVisible = false;
    private int _index = 1;

    public GaugeCardView(Context context) {
        super(context);
        _context = context;
    }

    public GaugeCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        _context = context;
    }

    public GaugeCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _context = context;
    }

    @AfterViews
    void afterViews(){
        List<Entry> entries = new ArrayList<>();

        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(getResources().getColor(R.color.colorAccent));
        dataSet.setLineWidth(2.0f);
        dataSet.setValueTextColor(getResources().getColor(R.color.colorAccent));

        _lineData = new LineData(dataSet);

        _chart.setData(_lineData);
        _chart.setDescription(null);
        _chart.getLegend().setEnabled(false);
        _chart.invalidate();

        setUpLeftYAxis();
        setUpXAxis();

        _chart.getAxisRight().setEnabled(false);
    }

    @Click({R.id.btnExpand})
    void onClick(View view) {
        if (_isChartVisible) {
            view.startAnimation(AnimationHelper.getRotateToAngle(45.0f, 0.0f));

            _isChartVisible = false;
            _chart.setVisibility(View.GONE);
        } else {
            view.startAnimation(AnimationHelper.getRotateToAngle(0.0f, 45.0f));

            _isChartVisible = true;
            _chart.setVisibility(View.VISIBLE);
        }
    }

    public void setValue(float value){
        _gaugeView.setValue(value);
        _value.setText(String.format(Locale.US, "%5.1f", value));

        _lineData.addEntry(new Entry(_index++, value), 0);
        _chart.notifyDataSetChanged();
        _chart.invalidate();
    }

    public void setText(int resId){
        _gaugeText.setText(getResources().getText(resId));
    }

    public void setUnits(int resId){
        _units.setText(getResources().getText(resId));
    }

    private void setUpXAxis() {
        XAxis axisX = _chart.getXAxis();
        axisX.setPosition(XAxisPosition.BOTTOM);
        axisX.setDrawAxisLine(false);
        axisX.setDrawGridLines(true);
        axisX.setTextColor(ContextCompat.getColor(_context, R.color.colorAccent));
        axisX.setGridColor(ContextCompat.getColor(_context, R.color.colorAccent));
        axisX.setCenterAxisLabels(true);
        axisX.setGridLineWidth(0.75f);
    }

    private void setUpLeftYAxis() {
        YAxis axis = _chart.getAxisLeft();
        axis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
        axis.setDrawGridLines(true);
        axis.setGranularityEnabled(true);
        axis.setGridLineWidth(0.75f);
        axis.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        axis.setGridColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        axis.setDrawLimitLinesBehindData(true);
    }
}
