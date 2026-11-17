package com.zargento.navidad

// cx, cy, rx, ry del hueco de la cara, como fracción del ANCHO de la imagen.
// Si una cara no cae bien en el hueco, ajusta estos números.
data class Tpl(val res: Int, val cx: Float, val cy: Float, val rx: Float, val ry: Float)

object Templates {
  val all = listOf(
    Tpl(R.drawable.t_pet1, 0.4554f, 0.4071f, 0.1875f, 0.1696f),
    Tpl(R.drawable.t_pet2, 0.3661f, 0.4321f, 0.1286f, 0.1393f),
    Tpl(R.drawable.t_pet3, 0.3556f, 0.4537f, 0.1204f, 0.1296f),
    Tpl(R.drawable.t_pet4, 0.4481f, 0.3648f, 0.1259f, 0.1259f),
    Tpl(R.drawable.t_pet5, 0.4592f, 0.5714f, 0.1939f, 0.1939f),
    Tpl(R.drawable.t_pet6, 0.4032f, 0.3274f, 0.1532f, 0.1452f),
    Tpl(R.drawable.t_elfa, 0.4094f, 0.3913f, 0.1449f, 0.1884f),
    Tpl(R.drawable.t_elfo, 0.5000f, 0.4325f, 0.1266f, 0.1308f)
  )
}
