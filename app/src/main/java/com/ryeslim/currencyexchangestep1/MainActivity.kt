package com.ryeslim.currencyexchangestep1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    var balance = arrayOf(BigDecimal(1000.00), BigDecimal(0.00), BigDecimal(0.00))
    var commission = arrayOf(BigDecimal(0.00), BigDecimal(0.00), BigDecimal(0.00))
    var currency = arrayOf("EUR", "USD", "JPY")
    var amountFrom = -1.00.toBigDecimal()
    var amountResult = 0.00.toBigDecimal()
    var indexFrom = -1
    var indexTo = -1
    var url = ""
    var thisCommission = 0.00.toBigDecimal()
    var numberOfOperations = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViews()
        convert.setOnClickListener { manageConversion() }
    }

    fun amountFrom() {
        if (amount_from.text.toString().trim().length > 0)
            amountFrom = amount_from.text.toString().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
        else amountFrom = -1.00.toBigDecimal()
    }

    fun currencyFrom() {
        indexFrom = when (radio_group_from.getCheckedRadioButtonId()) {
            from_eur.getId() -> 0
            from_usd.getId() -> 1
            from_jpy.getId() -> 2
            else -> -1
        }
    }

    fun currencyTo() {
        indexTo = when (radio_group_to.getCheckedRadioButtonId()) {
            to_eur.getId() -> 0
            to_usd.getId() -> 1
            to_jpy.getId() -> 2
            else -> -1
        }
    }

    fun manageConversion() {
        var errorMessage: String? = null

        numberOfOperations++

        amountFrom()
        currencyFrom()
        currencyTo()
        calculateCommission()

        if (amountFrom < 0.00.toBigDecimal()) {
            errorMessage = getString(R.string.enter_the_amount)
        } else if (radio_group_from.getCheckedRadioButtonId() == -1 || radio_group_to.getCheckedRadioButtonId() == -1 || indexFrom == indexTo) {
            errorMessage = getString(R.string.radio_button_error)
        } else if (amountFrom + thisCommission > balance[indexFrom]) {
            errorMessage = getString(R.string.insufficient_funds)
        }

        if (errorMessage != null) {
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
            numberOfOperations--
        } else {
            makeUrl()
            getResultFromNetwork()
            calculateValues()
            setViews()
            infoMessage()
        }
        return
    }

    fun calculateCommission() {
        if (numberOfOperations > 5) {
            thisCommission = zeroSevenPercent(amountFrom, 0.7.toBigDecimal())
        } else {
            thisCommission = 0.00.toBigDecimal()
        }
    }

    fun zeroSevenPercent(value: BigDecimal, percent: BigDecimal) = (value * percent / 100.00.toBigDecimal()).setScale(
        2, RoundingMode.HALF_EVEN
    )

    fun makeUrl() {
        url =
            "http://api.evp.lt/currency/commercial/exchange/$amountFrom-${currency[indexFrom]}/${currency[indexTo]}/latest"
    }

    fun getResultFromNetwork() {
        //go to url via retrofit/volley
        // and get result in JSON

        //some fake number
        amountResult = 50.00.toBigDecimal()
    }

    fun calculateValues() {
        balance[indexFrom] = balance[indexFrom] - amountFrom - thisCommission
        balance[indexTo] = balance[indexTo] + amountResult
        commission[indexFrom] += thisCommission
    }

    fun setViews() {
        radio_group_from.clearCheck()
        radio_group_to.clearCheck()
        amount_from.text.clear()
        eur_balance_value.text = String.format("%.2f", balance[0])
        usd_balance_value.text = String.format("%.2f", balance[1])
        jpy_balance_value.text = String.format("%.2f", balance[2])
        eur_commissions_value.text = String.format("%.2f", commission[0])
        usd_commissions_value.text = String.format("%.2f", commission[1])
        jpy_commissions_value.text = String.format("%.2f", commission[2])
    }

    fun infoMessage() {
        var infoMessage = String.format(
            "%s %.2f %s %s %.2f %s. %s %.2f %s",
            getString(R.string.you_converted),
            amountFrom,
            currency[indexFrom],
            getString(R.string.to),
            amountResult,
            currency[indexTo],
            getString(R.string.commissions_paid),
            thisCommission,
            currency[indexFrom]
        )
        info_message.text = infoMessage
    }
}
