package com.example.programacion2;
import android.content.Intent; import android.os.Bundle; import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.programacion2.ui.cliente.ClienteActivity; import com.example.programacion2.ui.archivos.ArchivosActivity;
public class MainActivity extends AppCompatActivity {
  @Override protected void onCreate(Bundle s){ super.onCreate(s); setContentView(R.layout.activity_main);
    Button b1=findViewById(R.id.btnCliente), b2=findViewById(R.id.btnArchivos);
    b1.setOnClickListener(v->startActivity(new Intent(this, ClienteActivity.class)));
    b2.setOnClickListener(v->startActivity(new Intent(this, ArchivosActivity.class)));
  }
}
