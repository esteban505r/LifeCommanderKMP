package utils

fun <T> List<T>.swap(index1: Int, index2: Int): List<T> {
    val newList = this.toMutableList()
    val tmp = newList[index1]
    newList[index1] = newList[index2]
    newList[index2] = tmp
    return newList
}