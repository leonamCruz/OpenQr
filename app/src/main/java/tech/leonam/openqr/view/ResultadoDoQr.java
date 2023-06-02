package tech.leonam.openqr.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import tech.leonam.openqr.R;

public class ResultadoDoQr extends AppCompatActivity {

    private Button copiar;
    private EditText texto;
    private AdView superior, inferior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_do_qr);
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(Color.BLACK);
        iniciarComponentes();
        iniciarAnuncios();
        inserirTextoDoQr();
        copiar();
    }

    private void copiar() {
        copiar.setOnClickListener(e->{
            var clipBoardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            var clip = ClipData.newPlainText("QrCode",texto.getText());
            clipBoardManager.setPrimaryClip(clip);
            Toast.makeText(this,getString(R.string.copiado_com_sucesso),Toast.LENGTH_LONG).show();
        });
    }

    public void inserirTextoDoQr() {
        var intencao = getIntent();
        texto.setText(intencao.getStringExtra("qr"));

    }

    public void iniciarComponentes() {
        copiar = findViewById(R.id.copiar);
        texto = findViewById(R.id.multiLine);
        superior = findViewById(R.id.sup);
        inferior = findViewById(R.id.inf);
    }

    public void iniciarAnuncios() {
        MobileAds.initialize(this, initializationStatus -> {
        });
        var requestOne = new AdRequest.Builder().build();
        var requestTwo = new AdRequest.Builder().build();
        var requestThree = new AdRequest.Builder().build();
        superior.loadAd(requestOne);
        inferior.loadAd(requestTwo);

        InterstitialAd.load(this, "ca-app-pub-8135010806374552/1627640697", requestThree, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                interstitialAd.show(ResultadoDoQr.this);
            }
        });
    }
}