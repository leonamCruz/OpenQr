package tech.leonam.openqr.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.FileNotFoundException;

import tech.leonam.openqr.R;

public class MainActivity extends AppCompatActivity {
    private Button tiraFoto;
    private Button galeria;
    private AdView superior;
    private AdView inferior;
    private TextureView view;
    private Camera camera;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 200;
    private ImageView lanterna;
    private Camera.Parameters parameters;
    private Boolean isLigado;


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
        clickNaLanterna();
        pedirPermissao();
    }

    public void clickNaLanterna() {
        lanterna.setOnClickListener(e -> {
            if (isLigado) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                isLigado = false;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                isLigado = true;
            }
        });
    }
    public void iniciarComponentes() {
        tiraFoto = findViewById(R.id.capturar);
        galeria = findViewById(R.id.galeria);
        superior = findViewById(R.id.adsSuperior);
        inferior = findViewById(R.id.adsInferior);
        view = findViewById(R.id.camera);
        lanterna = findViewById(R.id.lanterna);
        TextView texto = findViewById(R.id.textView2);
        isLigado = false;
        pedirPermissao();
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
                if (qr.equals("Not Found")) {
                    Toast.makeText(this, R.string.n_o_consegui_ler_seu_qr_code, Toast.LENGTH_LONG).show();
                } else {
                    var intencao = new Intent(this, ResultadoDoQr.class);
                    intencao.putExtra("qr", qr);
                    startActivity(intencao);
                }

            } catch (Exception r) {
                r.printStackTrace();
            }

        });
    }

    public void clickNaGaleria() {
        galeria.setOnClickListener(e -> {
            var intencao = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intencao,PICK_IMAGE_REQUEST_CODE);

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Obtenha a URI da imagem selecionada
            Uri imageUri = data.getData();

            try {
                // Decodifique a URI em um objeto Bitmap
                var bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                var qr = QRCodeDecoder.decodeQRCode(bitmap);
                if (qr.equals("Not Found")) {
                    Toast.makeText(this, getString(R.string.n_o_consegui_ler_seu_qr_code), Toast.LENGTH_LONG).show();
                } else {
                    var intencao = new Intent(this, ResultadoDoQr.class);
                    intencao.putExtra("qr", qr);
                    startActivity(intencao);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void criarPreview() {
        view.setSurfaceTextureListener(textureListener);
    }

    private SurfaceTextureListener textureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                configCam(width, height);
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
        parameters = camera.getParameters();
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
            }
        }
    }

    public Bitmap capturaImagem() {
        return view.getBitmap();
    }

    public void pedirPermissao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }
}