package me.jbusdriver.commen.bean

import java.io.Serializable

interface ILink : ICollectCategory, Serializable {
    val link: String
}