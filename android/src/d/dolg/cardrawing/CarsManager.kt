package d.dolg.cardrawing

object CarsManager {

    fun getCars() : ArrayList<Car> {
        val list = ArrayList<Car>()
        list.add(Car("car1", 1, true, 100, 50, 1))
        list.add(Car("car2", 2, true, 150, 10, 12))
        return list
    }

    fun getCarImageById(id: Int) = when(id) {
        1 -> R.drawable.car1
        else -> R.drawable.car1
    }

}