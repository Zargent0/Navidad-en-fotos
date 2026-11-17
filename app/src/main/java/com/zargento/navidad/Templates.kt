package com.zargento.navidad

// cx, cy, rx, ry del hueco de la cara, como fracción del ANCHO de la imagen.
// Si una cara no cae bien en el hueco, ajusta estos números.
data class Tpl(val res: Int, val cx: Float, val cy: Float, val rx: Float, val ry: Float)

object Templates {
  val all = listOf(
    Tpl(R.drawable.t_pet1, 0.4616f, 0.4420f, 0.1982f, 0.1964f),
    Tpl(R.drawable.t_pet2, 0.4429f, 0.4250f, 0.1991f, 0.1991f),
    Tpl(R.drawable.t_pet3, 0.4574f, 0.4380f, 0.2009f, 0.2019f),
    Tpl(R.drawable.t_pet4, 0.5343f, 0.4852f, 0.2093f, 0.2083f),
    Tpl(R.drawable.t_pet5, 0.4694f, 0.5571f, 0.2357f, 0.2378f),
    Tpl(R.drawable.t_pet6, 0.4129f, 0.4032f, 0.1798f, 0.1798f),
    Tpl(R.drawable.t_elfa, 0.4094f, 0.3913f, 0.1449f, 0.1884f),
    Tpl(R.drawable.t_elfo, 0.5000f, 0.4325f, 0.1266f, 0.1308f)
  )
}
