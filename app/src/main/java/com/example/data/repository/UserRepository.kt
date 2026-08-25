package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    
    fun getUserByIdFlow(userId: Int): Flow<User?> = userDao.getUserByIdFlow(userId)

    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
}
