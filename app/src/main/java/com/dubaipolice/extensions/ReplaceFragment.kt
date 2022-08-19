package com.dubaipolice.extensions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
    val fragmentTransaction = beginTransaction()
    fragmentTransaction.func()
    fragmentTransaction.commitAllowingStateLoss()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        add(frameId, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}

fun Fragment.addFragment(fragment: Fragment, frameId: Int) {
    activity?.supportFragmentManager?.inTransaction {
        add(frameId, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}

fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    frameId: Int
) {
    supportFragmentManager.inTransaction {
        replace(frameId, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}

fun AppCompatActivity.replaceFragmentWithoutBackStack(
    fragment: Fragment,
    frameId: Int
) {
    supportFragmentManager.inTransaction {
        replace(frameId, fragment)
    }
}

fun Fragment.replaceFragment(fragment: Fragment, frameId: Int) {
    activity?.supportFragmentManager?.inTransaction {
        replace(frameId, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}

fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    frameId: Int,
    args: Bundle
) {
    supportFragmentManager.inTransaction {
        fragment.arguments = args
        replace(frameId, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}

fun Fragment.replaceFragment(
    fragment: Fragment,
    frameId: Int,
    args: Bundle
) {
    activity?.supportFragmentManager?.inTransaction {
        fragment.arguments = args
        replace(frameId, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}