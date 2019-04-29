package lt.kepo.gaugeter.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    @ViewById(R.id.gaugeChart) LineChart _gaugeChart;

    private LineData _lineData;

    public GaugeCardView(Context context) {
        super(context);
    }

    public GaugeCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GaugeCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    void afterViews(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0.0f, 0.0f));

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextColor(getResources().getColor(R.color.white)); // styling, ...

        _lineData = new LineData(dataSet);
        _gaugeChart.setData(_lineData);
        _gaugeChart.invalidate(); // refresh
    }

    @Click({R.id.btnExpand})
    void onClick(View view) {
        if (_gaugeChart.getVisibility() != View.GONE) {
            view.startAnimation(AnimationHelper.getRotateToAngle(45.0f, 0.0f));

            _gaugeChart.setVisibility(View.GONE);
        } else {
            view.startAnimation(AnimationHelper.getRotateToAngle(0.0f, 45.0f));

            _gaugeChart.setVisibility(View.VISIBLE);
        }
    }

    public void setValue(float value){
        _gaugeView.setValue(value);
        _value.setText(String.format(Locale.US, "%5.1f", value));

        int _seconds = 0;
        _lineData.addEntry(new Entry(_seconds, value), 0);
        _gaugeChart.notifyDataSetChanged();
        _gaugeChart.invalidate();
    }

    public void setText(int resId){
        _gaugeText.setText(getResources().getText(resId));
    }

    public void setUnits(int resId){
        _units.setText(getResources().getText(resId));
    }
}
