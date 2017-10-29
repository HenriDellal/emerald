/***
  Copyright (c) 2008-2013 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.commonsware.cwac.colormixer;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ru.henridellal.emerald.R;

public class ColorMixer extends RelativeLayout {
  private static final String SUPERSTATE="superState";
  private static final String COLOR="color";
  private View swatch=null;
  private SeekBar alpha=null;
  private SeekBar red=null;
  private SeekBar blue=null;
  private SeekBar green=null;
  private TextView alphaValue, redValue, greenValue, blueValue;
  private OnColorChangedListener listener=null;

  public ColorMixer(Context context) {
    super(context);

    initMixer(null);
  }

  public ColorMixer(Context context, AttributeSet attrs) {
    super(context, attrs);

    initMixer(attrs);
  }

  public ColorMixer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    initMixer(attrs);
  }

  public OnColorChangedListener getOnColorChangedListener() {
    return(listener);
  }

  public void setOnColorChangedListener(OnColorChangedListener listener) {
    this.listener=listener;
  }

  public int getColor() {
    return(Color.argb(alpha.getProgress(), red.getProgress(), green.getProgress(),
                      blue.getProgress()));
  }

  public void setColor(int color) {
    alpha.setProgress(Color.alpha(color));
    alphaValue.setText(((Integer)Color.alpha(color)).toString());
    red.setProgress(Color.red(color));
    redValue.setText(((Integer)Color.red(color)).toString());
    green.setProgress(Color.green(color));
    greenValue.setText(((Integer)Color.green(color)).toString());
    blue.setProgress(Color.blue(color));
    blueValue.setText(((Integer)Color.blue(color)).toString());
  }

  private void initMixer(AttributeSet attrs) {
    if (isInEditMode()) {
      return;
    }
    
    LayoutInflater inflater=null;

    if (getContext() instanceof Activity) {
      inflater=((Activity)getContext()).getLayoutInflater();
    }
    else {
      inflater=LayoutInflater.from(getContext());
    }

    inflater.inflate(R.layout.cwac_colormixer_main, this, true);

    swatch=findViewById(R.id.swatch);

    alpha=(SeekBar)findViewById(R.id.alpha);
    alphaValue=(TextView)findViewById(R.id.alphaValue);
    alpha.setMax(0xFF);
    alpha.setOnSeekBarChangeListener(onMix);
    
    red=(SeekBar)findViewById(R.id.red);
    redValue=(TextView)findViewById(R.id.redValue);
    red.setMax(0xFF);
    red.setOnSeekBarChangeListener(onMix);

    green=(SeekBar)findViewById(R.id.green);
    greenValue=(TextView)findViewById(R.id.greenValue);
    green.setMax(0xFF);
    green.setOnSeekBarChangeListener(onMix);

    blue=(SeekBar)findViewById(R.id.blue);
    blueValue=(TextView)findViewById(R.id.blueValue);
    blue.setMax(0xFF);
    blue.setOnSeekBarChangeListener(onMix);

    if (attrs != null) {
      int[] styleable=R.styleable.ColorMixer;
      TypedArray a=
          getContext().obtainStyledAttributes(attrs, styleable, 0, 0);

      setColor(a.getInt(R.styleable.ColorMixer_cwac_colormixer_color, 0xFFA4C639));
      a.recycle();
    }
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Bundle state=new Bundle();

    state.putParcelable(SUPERSTATE, super.onSaveInstanceState());
    state.putInt(COLOR, getColor());

    return(state);
  }

  @Override
  public void onRestoreInstanceState(Parcelable ss) {
    Bundle state=(Bundle)ss;

    super.onRestoreInstanceState(state.getParcelable(SUPERSTATE));

    setColor(state.getInt(COLOR));
  }

  private SeekBar.OnSeekBarChangeListener onMix=
      new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
          int color=getColor();
          switch (seekBar.getId()) {
            case R.id.alpha:
              alphaValue.setText(((Integer)Color.alpha(color)).toString());
              break;
            case R.id.red:
              redValue.setText(((Integer)Color.red(color)).toString());
              break;
            case R.id.green:
              greenValue.setText(((Integer)Color.green(color)).toString());
              break;
            case R.id.blue:
              blueValue.setText(((Integer)Color.blue(color)).toString());
              break;
          }

          swatch.setBackgroundColor(color);

          if (listener != null) {
            listener.onColorChange(color);
          }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
          // unused
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
          // unused
        }
      };

  public interface OnColorChangedListener {
    public void onColorChange(int argb);
  }
}
