package com.sscommu.pokeumcho;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeMainFragment extends Fragment {

    private Button btnGitHubLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_main_home,
                container, false);

        btnGitHubLink = view.findViewById(R.id.btnGitHubLink);
        btnGitHubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a URL in Android's web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/PoKeumCho/SScommu"));
                startActivity(browserIntent);
            }
        });

        return view;
    }
}
