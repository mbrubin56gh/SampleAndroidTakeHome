package com.example.sampletakehome.repository

import com.example.sampletakehome.database.UserEntity
import com.example.sampletakehome.database.UsersDatabase
import com.example.sampletakehome.dependencygraph.AppScope
import com.example.sampletakehome.networking.UserNetworkModel
import com.example.sampletakehome.networking.UsersService
import com.example.sampletakehome.repository.UsersRepository.UsersResult.Success
import com.example.sampletakehome.repository.UsersRepository.UsersResult.WithNetworkError
import com.squareup.anvil.annotations.ContributesTo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ContributesTo(AppScope::class)
interface UsersRepositoryComponentInterface {
    fun usersRepository(): UsersRepository
}

data class User(
    val id: Long,
    val firstName: String,
    val imageUrl: String
)

class UsersRepository @Inject constructor(
    private val usersService: UsersService,
    private val usersDatabase: UsersDatabase
) {
    sealed class UsersResult {
        abstract val users: List<User>

        data class WithNetworkError(override val users: List<User>) : UsersResult()
        data class Success(override val users: List<User>) : UsersResult()
    }

    private var networkError = false

    suspend fun refreshUsers() {
        try {
            val users = usersService.users()
            networkError = false
            usersDatabase.userDao().insertAll(users.users.toUserEntities())
        } catch (e: Exception) {
            networkError = true
        }
    }

    fun users(): Flow<UsersResult> = usersDatabase.userDao().getAll()
        .map {
            it.toUsers()
                .let { users -> if (networkError) WithNetworkError(users) else Success(users) }
        }.distinctUntilChanged()

    suspend fun getUser(userId: Long) = usersDatabase.userDao().getOne(userId).toUser()
}

private fun UserEntity.toUser(): User = User(id = id, firstName = firstName, imageUrl = imageUrl)

private fun List<UserEntity>.toUsers() = map { it.toUser() }

private fun UserNetworkModel.toUserEntity() = UserEntity(
    id = id,
    firstName = firstName,
    imageUrl = imageUrl
)

private fun List<UserNetworkModel>.toUserEntities() = map { it.toUserEntity() }
