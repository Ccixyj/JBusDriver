package me.jbusdriver.common.bean

import java.io.Serializable

interface ILink : ICollectCategory, Serializable {
    val link: String
}