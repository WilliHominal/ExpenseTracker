package com.warh.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import com.warh.expensetracker.widget.BalanceWidgetRefreshWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //BalanceWidgetRefreshWorker.enqueue(this)

        setContent { App() }
    }
}