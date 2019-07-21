package d.dolg.cardrawing.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import d.dolg.cardrawing.Car
import d.dolg.cardrawing.CarsManager
import d.dolg.cardrawing.Cnst
import d.dolg.cardrawing.R

class MainMenuActivity : AppCompatActivity() {
    private var selectedCar: Car? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parentView = LayoutInflater.from(this).inflate(R.layout.activity_main_menu, null)
        setContentView(parentView)

        val bStartOfflineGame = findViewById<Button>(R.id.b_start_offline_game)
        val listCardCar = findViewById<LinearLayout>(R.id.list_cars_card)


        val listCars = CarsManager.getCars()
        listCars.forEach {
            listCardCar.addView(generateCarCard(it, listCardCar))
        }
        tuneDefaultCar(listCars[0])

        bStartOfflineGame.setOnClickListener {
            startDrawingActivity(Cnst.GameTypes.SingleMode)
        }
    }


    private fun generateCarCard(car: Car, parent: LinearLayout) : View {
        val card = LayoutInflater.from(this).inflate(R.layout.car_card, parent, false)
        val imageView = card.findViewById<ImageView>(R.id.image_car)
        val speedProgress = card.findViewById<LinearLayout>(R.id.speed_progress)
        val selectedView = card.findViewById<ImageView>(R.id.image_car_check_mark)

        imageView.setImageResource(CarsManager.getCarImageById(car.id))

        card.setOnClickListener {
            if(car.isOpen) {
                val lastSelectedCarView = selectedCar?.cardView?.findViewById<ImageView>(R.id.image_car_check_mark)
                lastSelectedCarView?.visibility = ImageView.INVISIBLE
                selectedCar = car
                selectedView.visibility = ImageView.VISIBLE
            }
        }

        car.cardView = card
        return card
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun tuneDefaultCar(car: Car) {
        car.cardView?.findViewById<ImageView>(R.id.image_car_check_mark)
                ?.visibility = ImageView.VISIBLE
        selectedCar = car
    }

    private fun startDrawingActivity(gameType: Cnst.GameTypes) {
        val intent = Intent()
        intent.setClass(this, DrawingTrackActivity::class.java)
        intent.putExtra(Cnst.game_type, gameType)
        startActivity(intent)
    }
}