package com.app.simpleplayer.presentation.utils

import android.content.Context
import android.widget.Toast

fun showShortToast(context: Context, text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
