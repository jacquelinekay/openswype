package org.pocketworkstation.pckeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main extends Activity {
	Context mContext;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mContext = this;
        
        final Button practice = (Button) findViewById(R.id.main_practice_btn);
        practice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(mContext, SettingsActivity.class));
            }
        });
        
        final Button setup1 = (Button) findViewById(R.id.main_settings_btn);
        setup1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(mContext, SettingsActivity.class));
            }
        });

        
	}
}
