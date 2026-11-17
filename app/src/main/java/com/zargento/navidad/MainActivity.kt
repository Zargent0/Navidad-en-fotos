package com.zargento.navidad

import android.content.*
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import java.io.File

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: android.os.Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MaterialTheme(colorScheme = darkColorScheme()) { Surface(Modifier.fillMaxSize()) { App() } } }
  }
}

@Composable
fun App() {
  val ctx = LocalContext.current
  var tpl by remember { mutableStateOf<Tpl?>(null) }
  var face by remember { mutableStateOf<Bitmap?>(null) }
  var msg by remember { mutableStateOf<String?>(null) }
  val photoUri = remember { FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", File(ctx.cacheDir, "foto.jpg")) }

  fun process(uri: Uri) {
    val bmp = ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri)) { d, info, _ ->
      d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
      val m = maxOf(info.size.width, info.size.height)
      if (m > 1600) d.setTargetSampleSize(m / 1600 + 1)
    }
    FaceDetection.getClient().process(InputImage.fromBitmap(bmp, 0))
      .addOnSuccessListener { faces ->
        val f = faces.maxByOrNull { it.boundingBox.width() }
        if (f == null) msg = "No encontré una cara, intenta con otra foto" else { face = cropFace(bmp, f.boundingBox); msg = null }
      }
      .addOnFailureListener { msg = "No pude detectar la cara" }
  }
  val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) process(photoUri) }
  val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u -> if (u != null) process(u) }

  val t = tpl
  val f = face
  when {
    t == null -> Column(Modifier.padding(12.dp)) {
      Text("Elige una plantilla", style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(8.dp))
      LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(Templates.all) { x -> Image(painterResource(x.res), null, Modifier.fillMaxWidth().clickable { tpl = x }) }
      }
    }
    f == null -> Column(Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
      Image(painterResource(t.res), null, Modifier.weight(1f))
      msg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button({ camera.launch(photoUri) }) { Text("Tomar foto") }
        Button({ gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Galería") }
        OutlinedButton({ tpl = null; msg = null }) { Text("Volver") }
      }
    }
    else -> Result(t, f) { face = null }
  }
}

@Composable
fun Result(t: Tpl, face: Bitmap, onBack: () -> Unit) {
  val ctx = LocalContext.current
  val base = remember(t) { BitmapFactory.decodeResource(ctx.resources, t.res) }
  var s by remember { mutableFloatStateOf(1.1f) }
  var dx by remember { mutableFloatStateOf(0f) }
  var dy by remember { mutableFloatStateOf(0f) }
  var box by remember { mutableStateOf(IntSize.Zero) }
  val out = remember(t, face, s, dx, dy) { render(base, face, t, s, dx, dy) }
  val k = if (box.width == 0) 1f else 1f / minOf(box.width.toFloat() / base.width, box.height.toFloat() / base.height)

  Column(Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
    Image(out.asImageBitmap(), null, Modifier.weight(1f).fillMaxWidth().onSizeChanged { box = it }
      .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> s = (s * zoom).coerceIn(0.5f, 3f); dx += pan.x * k; dy += pan.y * k } })
    Text("Arrastra y pellizca para ajustar la cara")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button({ if (save(ctx, out) != null) android.widget.Toast.makeText(ctx, "Guardada en Galería", android.widget.Toast.LENGTH_SHORT).show() }) { Text("Guardar") }
      Button({
        save(ctx, out)?.let { u ->
          ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, u).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), null))
        }
      }) { Text("Compartir") }
      OutlinedButton(onBack) { Text("Otra foto") }
    }
  }
}

fun cropFace(b: Bitmap, r: Rect): Bitmap {
  val side = minOf((maxOf(r.width(), r.height()) * 1.25f).toInt(), b.width, b.height)
  val l = (r.centerX() - side / 2).coerceIn(0, b.width - side)
  val t = (r.centerY() - side / 2).coerceIn(0, b.height - side)
  return Bitmap.createBitmap(b, l, t, side, side)
}

fun render(t: Bitmap, f: Bitmap, h: Tpl, s: Float, dx: Float, dy: Float): Bitmap {
  val out = t.copy(Bitmap.Config.ARGB_8888, true)
  val c = Canvas(out)
  val w = out.width.toFloat()
  val cx = h.cx * w; val cy = h.cy * w
  val rx = h.rx * w * 1.05f; val ry = h.ry * w * 1.05f
  c.save()
  c.clipPath(Path().apply { addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW) })
  val half = maxOf(rx, ry) * s
  c.drawBitmap(f, null, RectF(cx + dx - half, cy + dy - half, cx + dx + half, cy + dy + half), Paint(Paint.FILTER_BITMAP_FLAG))
  c.restore()
  return out
}

fun save(ctx: Context, b: Bitmap): Uri? {
  val v = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "navidad_${System.currentTimeMillis()}.png")
    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NavidadFotos")
  }
  val u = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v) ?: return null
  ctx.contentResolver.openOutputStream(u)?.use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
  return u
}
