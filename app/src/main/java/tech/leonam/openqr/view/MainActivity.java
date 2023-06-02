package tech.leonam.openqr.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import tech.leonam.openqr.R;

public class MainActivity extends AppCompatActivity {
    private Button tiraFoto;
    private Button galeria;
    private AdView superior;
    private AdView inferior;
    private TextureView view;
    private Camera camera;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(Color.BLACK);
        iniciarComponentes();
        iniciarAnuncios();
        clickNaCamera();
        clickNaGaleria();
        criarPreview();
    }

    public void iniciarComponentes() {
        tiraFoto = findViewById(R.id.capturar);
        galeria = findViewById(R.id.galeria);
        superior = findViewById(R.id.adsSuperior);
        inferior = findViewById(R.id.adsInferior);
        view = findViewById(R.id.camera);
        camera = Camera.open();
    }

    public void iniciarAnuncios() {
        MobileAds.initialize(this, initializationStatus -> {
        });
        var requestUm = new AdRequest.Builder().build();
        var requestDois = new AdRequest.Builder().build();
        superior.loadAd(requestUm);
        inferior.loadAd(requestDois);
    }

    public void clickNaCamera() {
        tiraFoto.setOnClickListener(e -> {
            try {
                var bitmap = capturaImagem();
                var qr = QRCodeDecoder.decodeQRCode(bitmap);
                System.out.println(qr);

            }catch (Exception r){
                r.printStackTrace();
            }

        });
    }

    public void clickNaGaleria() {
        galeria.setOnClickListener(e -> {

        });
    }

    public void criarPreview() {
        view.setSurfaceTextureListener(textureListener);
    }

    private SurfaceTextureListener textureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                configCam(width,height);
                openCamera();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            releaseCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        }
    };

    private void configCam(int viewWidth, int viewHeight) {
            var parameters = camera.getParameters();
            Camera.Size bestSize = null;
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            var supportedSizes = parameters.getSupportedPreviewSizes();
            float targetRatio = (float) viewWidth / viewHeight;

            for (Camera.Size size : supportedSizes) {
                float ratio = (float) size.width / size.height;
                if (Math.abs(ratio - targetRatio) <= 0.1) {
                    if (bestSize == null) {
                        bestSize = size;
                    } else {
                        int currentArea = bestSize.width * bestSize.height;
                        int newArea = size.width * size.height;
                        if (newArea > currentArea) {
                            bestSize = size;
                        }
                    }
                }
            }

            if (bestSize != null) {
                parameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(parameters);
            }
    }

    private void openCamera() {
        try {
            camera.setDisplayOrientation(90);
            camera.setPreviewTexture(view.getSurfaceTexture());
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.pedido);
                alert.setMessage(R.string.pedido);
                alert.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> finish());
                alert.create().show();
            }
        }
    }
    public Bitmap capturaImagem(){
        return view.getBitmap();
    }
}