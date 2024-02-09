package com.manchuan.tools.activity.normal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.crazylegend.viewbinding.viewBinding
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityDecimalConvertBinding
import java.util.Locale


class DecimalConvertActivity : BaseActivity(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private val binding by viewBinding(ActivityDecimalConvertBinding::inflate)

    // 定义下拉列表需要显示的单位名称
    private val nameArray = arrayOf(
        "二进制", "八进制", "十进制", "十六进制"
    )

    // 定义下拉列表需要显示的单位数组
    private val unitArray = arrayOf(
        "BIN", "OCT", "DEC", "HEX"
    )

    // 单位一
    private var unit1 = "十进制"

    // 单位二
    private var unit2 = "十六进制"

    // 数值一
    private var value1 = "0"

    // 数值二
    private var value2 = "0"

    // 临时数值
    private var tempValue: Long = 0
    private val buttonList = arrayListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "进制转换工具"
            setDisplayHomeAsUpEnabled(true)
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        buttonList.add(binding.btn0)
        buttonList.add(binding.btn1)
        buttonList.add(binding.btn2)
        buttonList.add(binding.btn3)
        buttonList.add(binding.btn4)
        buttonList.add(binding.btn5)
        buttonList.add(binding.btn6)
        buttonList.add(binding.btn7)
        buttonList.add(binding.btn8)
        buttonList.add(binding.btn9)
        buttonList.add(binding.btnA)
        buttonList.add(binding.btnB)
        buttonList.add(binding.btnC)
        buttonList.add(binding.btnD)
        buttonList.add(binding.btnE)
        buttonList.add(binding.btnF)
        buttonList.add(binding.btnClr)

        // 给按钮设置的点击事件
        // 给按钮设置的点击事件
        for (i in buttonList.indices) {
            if (i in 10..15) {
                buttonList[i].setTextColor(Color.parseColor("#FFD6D6D6"))
            }
            buttonList[i].setOnClickListener(this)
        }

        // 声明一个映射对象的列表，用于保存名称与单位配对信息
        // 声明一个映射对象的列表，用于保存名称与单位配对信息
        val list: MutableList<Map<String, Any>> = ArrayList()
        // name是名称，unit是单位
        // name是名称，unit是单位
        for (i in nameArray.indices) {
            val item: MutableMap<String, Any> = HashMap()
            item["name"] = nameArray[i]
            item["unit"] = unitArray[i]
            list.add(item)
        }

        // 声明一个下拉列表的简易适配器，其中指定了名称与单位两组数据
        // 声明一个下拉列表的简易适配器，其中指定了名称与单位两组数据
        val adapter = SimpleAdapter(
            this,
            list,
            R.layout.item_value_conversion,
            arrayOf("name", "unit"),
            intArrayOf(R.id.tv_name, R.id.tv_unit)
        )
        binding.spSelect1.setAdapter(adapter)
        binding.spSelect2.setAdapter(adapter)
        // 设置下拉列表默认显示
        // 设置下拉列表默认显示
        binding.spSelect1.setSelection(2)
        binding.spSelect2.setSelection(3)
        // 给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的 onItemSelected 方法
        // 给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的 onItemSelected 方法
        binding.spSelect1.onItemSelectedListener = this
        binding.spSelect2.onItemSelectedListener = this

        operation()
        refreshText()
    }

    // 清空并初始化
    private fun clear() {
        value2 = "0"
        value1 = value2
        refreshText()
    }

    // 刷新文本显示
    private fun refreshText() {
        binding.tvValue1.text = value1
        binding.tvValue2.text = value2
    }

    // 回退
    private fun delete() {
        if (value1.isNotEmpty()) {
            value1 = value1.substring(0, value1.length - 1)
            if (value1.isEmpty()) value1 = "0"
            operation()
            refreshText()
        }
    }

    // 运算
    private fun operation() {
        if (unit1 == unit2) {
            value2 = value1
            return
        }
        when (unit1) {
            "二进制" -> tempValue = value1.toLong(2)
            "八进制" -> tempValue = value1.toLong(8)
            "十进制" -> tempValue = value1.toLong()
            "十六进制" -> tempValue = value1.toLong(16)
        }
        when (unit2) {
            "二进制" -> value2 = java.lang.Long.toBinaryString(tempValue)
            "八进制" -> value2 = java.lang.Long.toOctalString(tempValue)
            "十进制" -> value2 = tempValue.toString()
            "十六进制" -> value2 = java.lang.Long.toHexString(tempValue)
        }
        value2 = value2.uppercase(Locale.getDefault())
    }

    override fun onClick(v: View?) {
        if (v!!.id !== R.id.iv_del && v!!.id !== R.id.btn_clr) {
            if ((unit1 == "二进制" || unit1 == "八进制") && value1.length >= 20) return else if (unit1 == "十进制" && value1.length >= 18) return else if (unit1 == "十六进制" && value1.length >= 15) return
        }

        var inputText = ""
        // 如果不是删除按钮和返回按钮
        // 如果不是删除按钮和返回按钮
        if (v!!.id !== R.id.iv_del) {
            inputText = (v as TextView).getText().toString()
        }

        when (v!!.id) {
            R.id.btn_clr -> clear()
            R.id.iv_del -> delete()
            R.id.btn_f, R.id.btn_e, R.id.btn_d, R.id.btn_c, R.id.btn_b, R.id.btn_a -> {
                if (unit1 == "二进制" || unit1 == "八进制" || unit1 == "十进制") return
                if (unit1 == "二进制" || unit1 == "八进制") return
                if (unit1 == "二进制") return
                value1 = if (value1 == "0") {
                    inputText
                } else {
                    value1 + inputText
                }
                operation()
                refreshText()
            }

            R.id.btn_9, R.id.btn_8 -> {
                if (unit1 == "二进制" || unit1 == "八进制") return
                if (unit1 == "二进制") return
                value1 = if (value1 == "0") {
                    inputText
                } else {
                    value1 + inputText
                }
                operation()
                refreshText()
            }

            R.id.btn_7, R.id.btn_6, R.id.btn_5, R.id.btn_4, R.id.btn_3, R.id.btn_2 -> {
                if (unit1 == "二进制") return
                value1 = if (value1 == "0") {
                    inputText
                } else {
                    value1 + inputText
                }
                operation()
                refreshText()
            }

            R.id.btn_1, R.id.btn_0 -> {
                value1 = if (value1 == "0") {
                    inputText
                } else {
                    value1 + inputText
                }
                operation()
                refreshText()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //TODO("Not yet implemented")
        val tv_unit: TextView? = parent?.findViewById(R.id.tv_unit)
        if (tv_unit != null) {
            tv_unit.visibility = GridView.GONE
        }

        if (parent?.getId() === R.id.sp_select1) {
            binding.tvUnit1.text = unitArray[position]
            unit1 = nameArray[position]
            value2 = "0"
            value1 = value2
            refreshText()
            when (position) {
                0 -> {
                    var i1 = 0
                    while (i1 < buttonList.size) {
                        if (i1 < 2) buttonList[i1].setTextColor(Color.parseColor("#FF000000")) else if (i1 >= 2 && i1 <= 15) buttonList[i1].setTextColor(
                            Color.parseColor("#FFD6D6D6")
                        )
                        i1++
                    }
                }

                1 -> {
                    var i1 = 0
                    while (i1 < buttonList.size) {
                        if (i1 < 8) buttonList[i1].setTextColor(Color.parseColor("#FF000000")) else if (i1 >= 8 && i1 <= 15) buttonList[i1].setTextColor(
                            Color.parseColor("#FFD6D6D6")
                        )
                        i1++
                    }
                }

                2 -> {
                    var i1 = 0
                    while (i1 < buttonList.size) {
                        if (i1 < 10) buttonList[i1].setTextColor(Color.parseColor("#FF000000")) else if (i1 >= 10 && i1 <= 15) buttonList[i1].setTextColor(
                            Color.parseColor("#FFD6D6D6")
                        )
                        i1++
                    }
                }

                3 -> {
                    var i1 = 0
                    while (i1 < buttonList.size) {
                        if (i1 < 16) buttonList[i1].setTextColor(Color.parseColor("#FF000000"))
                        i1++
                    }
                }
            }
        } else {
            binding.tvUnit2.text = unitArray[position]
            unit2 = nameArray[position]
            operation()
            refreshText()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //TODO("Not yet implemented")
    }
}