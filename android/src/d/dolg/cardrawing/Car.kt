package d.dolg.cardrawing

import android.view.View

data class Car(val name: String,
               val id: Int,
               val isOpen: Boolean,
               val maxSpeed: Int,
               val weight: Int,
               val friction: Int) {
    var cardView: View? = null
}