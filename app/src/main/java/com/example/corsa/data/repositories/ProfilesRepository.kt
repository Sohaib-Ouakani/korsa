package com.example.corsa.data.repositories

import com.example.corsa.data.model.Follow
import com.example.corsa.data.model.FollowInsert
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.data.model.Run
import com.example.corsa.ui.composables.UserEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.String
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ── Interface ──────────────────────────────────────────────────────────────
interface ProfilesRepository {
    suspend fun getProfileByUserId(userId: String, cachedValue: Boolean? = true): Profile
    suspend fun getUserEntryByUserId(userId: String): UserEntry
    suspend fun getAllProfiles(): List<Profile>
    suspend fun getMyProfile(cachedValue: Boolean? = true): Profile
    suspend fun updateProfile(update: ProfileUpdate): Profile
    suspend fun getMyUserEntry(): UserEntry
    suspend fun getProfileIFollow(cachedValue: Boolean? = true): List<Profile>
    suspend fun weeklyKmByUserId(userId: String): Float
    suspend fun getProfilesIDoNotFollow(cachedValue: Boolean? = true): List<Profile>
    suspend fun getIfIFollowAProfileByUserId(userId: String): Boolean
    suspend fun AddFollowToAProfileByUserId(userId: String)
    suspend fun StopFollowToAProfileByUserId(userId: String)
    suspend fun uploadAvatar(imageBytes: ByteArray, mimeType: String): String
    fun avatarUrl(path: String): String
}

// ── Fake implementation ────────────────────────────────────────────────────
class ProfilesRepositoryImpl(
    private val supabase: SupabaseClient
) : ProfilesRepository {

    private val ttlMs = 10 * 60 * 1000L
    private val _getProfileByUserIdTimeStamp = mutableMapOf<String, Long>()
    private val _getProfileByUserIdCache = mutableMapOf<String, Profile>()
    private val _getProfilesIDoNotFollowTimestamp = mutableMapOf<String, Long>()
    private val _getProfilesIDoNotFollowCache = mutableMapOf<String, List<Profile>>()
    private val _getProfileIFollowTimestamp = mutableMapOf<String, Long>()
    private val _getProfileIFollowCache = mutableMapOf<String, List<Profile>>()
    private var _getMyProfileTimestamp: Long? = null
    private var _getMyProfileCache: Profile? = null

    private var _wasFollowANewUserNotFriend: Boolean = false
    private var _wasFollowANewUserFriend: Boolean = false


    override suspend fun getProfileByUserId(userId: String, cachedValue: Boolean?): Profile {
        val cachedProfile = _getProfileByUserIdCache[userId]
        val timestamp = _getProfileByUserIdTimeStamp[userId]

        if (cachedValue == true &&
            cachedProfile != null &&
            timestamp != null &&
            System.currentTimeMillis() - timestamp < ttlMs
        ) {
            return cachedProfile
        }

        val profile = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<Profile>()

        _getProfileByUserIdCache[userId] = profile
        _getProfileByUserIdTimeStamp[userId] = System.currentTimeMillis()

        return profile
    }

    override suspend fun weeklyKmByUserId(userId: String): Float {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(zone).date

        // Get the start of the current week (Monday)
        val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val startInstant = startOfWeek.atStartOfDayIn(zone)

        val runs = supabase
            .from("runs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("start_time", startInstant.toString())
                }
            }
            .decodeList<Run>()

        return runs.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f
    }

    override suspend fun getProfilesIDoNotFollow(cachedValue: Boolean? ): List<Profile> {
        val currentProfileId = getMyProfile().id
        val cachedProfiles = _getProfilesIDoNotFollowCache[currentProfileId]
        val timestamp = _getProfilesIDoNotFollowTimestamp[currentProfileId]

        if (cachedValue == true &&
                cachedProfiles != null &&
                timestamp != null &&
                System.currentTimeMillis() - timestamp < ttlMs &&
                !_wasFollowANewUserNotFriend
        ) {
            return cachedProfiles
        }

        val follows = supabase.postgrest["follows"]
            .select {
                filter {
                    eq("follower_id", currentProfileId)
                }
            }
            .decodeList<Follow>()

        val excludedIds = (follows.map { it.followingId } + currentProfileId).toSet()

        val allProfiles = supabase.postgrest["profiles"]
            .select()
            .decodeList<Profile>()

        val result = allProfiles.filter { it.id !in excludedIds }

        _getProfilesIDoNotFollowCache[currentProfileId] = result
        _getProfilesIDoNotFollowTimestamp[currentProfileId] = System.currentTimeMillis()
        _wasFollowANewUserNotFriend = false
        return result
    }

    override suspend fun getIfIFollowAProfileByUserId(userId: String): Boolean {
        return getProfileIFollow().map { it.id }.contains(userId)
    }



    override suspend fun getProfileIFollow(cachedValue: Boolean?): List<Profile> {
        val currentUserId = getMyProfile().id
        val cachedProfiles = _getProfileIFollowCache[currentUserId]
        val timestamp = _getProfileIFollowTimestamp[currentUserId]

        if (cachedValue == true &&
            cachedProfiles != null &&
            timestamp != null &&
            System.currentTimeMillis() - timestamp < ttlMs &&
            !_wasFollowANewUserFriend
        ) {
            return cachedProfiles
        }

        val follows = supabase.postgrest["follows"]
            .select {
                filter {
                    eq("follower_id", currentUserId)
                }
            }
            .decodeList<Follow>()

        val followingIds = follows.map { it.followingId }

        if (followingIds.isEmpty()) {
            _getProfileIFollowCache[currentUserId] = emptyList()
            _getProfileIFollowTimestamp[currentUserId] = System.currentTimeMillis()
            return emptyList()
        }

        val result = supabase.postgrest["profiles"]
            .select {
                filter {
                    isIn("id", followingIds)
                }
            }
            .decodeList<Profile>()

        _getProfileIFollowCache[currentUserId] = result
        _getProfileIFollowTimestamp[currentUserId] = System.currentTimeMillis()
        _wasFollowANewUserFriend = false
        return result
    }

    override suspend fun AddFollowToAProfileByUserId(userId: String) {
        val currentProfileId = getMyProfile().id
        _wasFollowANewUserFriend = true
        _wasFollowANewUserNotFriend = true
        supabase.postgrest["follows"]
            .insert(FollowInsert(followerId = currentProfileId, followingId = userId))
    }

    override suspend fun StopFollowToAProfileByUserId(userId: String) {
        val currentProfileId = getMyProfile().id
        _wasFollowANewUserFriend = true
        _wasFollowANewUserNotFriend = true
        supabase.postgrest["follows"]
            .delete {
                filter {
                    eq("follower_id", currentProfileId)
                    eq("following_id", userId)
                }
            }
    }

    override suspend fun getUserEntryByUserId(userId: String): UserEntry {
        val profile = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<Profile>()

        val weeklyKm = weeklyKmByUserId(profile.id)
        val avatarUrl = if (profile.avatarPath != null) avatarUrl(profile.avatarPath) else null
        return UserEntry(
            profile.username,
            avatarUrl,
            weeklyKm,
            profile.level,
            profile.completedChallenges,
            profile.totalKm,
        )
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return supabase.postgrest["profiles"]
            .select()
            .decodeList<Profile>()
    }

    override suspend fun getMyProfile(cachedValue: Boolean? ): Profile {
        val cachedProfile = _getMyProfileCache
        val timestamp = _getMyProfileTimestamp

        if (cachedValue == true &&
            cachedProfile != null &&
            timestamp != null &&
            System.currentTimeMillis() - timestamp < ttlMs
        ) {
            return cachedProfile
        }

        val authUserId = getMyAuthUserId()

        val result = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("auth_user_id", authUserId)
                }
            }
            .decodeSingle<Profile>()

        _getMyProfileCache = result
        _getMyProfileTimestamp = System.currentTimeMillis()

        return result
    }

    override suspend fun updateProfile(update: ProfileUpdate): Profile {
        val authUserId = getMyAuthUserId()

        return supabase.postgrest["profiles"]
            .update(update) {
                filter {
                    eq("auth_user_id", authUserId)
                }
                select()
            }
            .decodeSingle<Profile>()
    }

    override suspend fun getMyUserEntry(): UserEntry {
        val profile = getMyProfile()
        val weeklyKm = weeklyKmByUserId(profile.id)
        val avatarUrl = if (profile.avatarPath != null) avatarUrl(profile.avatarPath) else null
        return UserEntry(
            profile.username,
            avatarUrl,
            weeklyKm,
            profile.level,
            profile.completedChallenges,
            profile.totalKm,
        )
    }



    @OptIn(ExperimentalUuidApi::class)
    override suspend fun uploadAvatar(imageBytes: ByteArray, mimeType: String): String {
        val userId = getMyAuthUserId()
        val extension = if (mimeType == "image/png") "png" else "jpg"

        // Delete old avatar if exists
        runCatching {
            val old = getMyProfile().avatarPath
            if (old != null) supabase.storage.from("avatars").delete(listOf(old))
        }

        val path = "$userId/avatar-${Uuid.random()}.$extension"

        supabase.storage
            .from("avatars")
            .upload(path, imageBytes) {
                contentType = ContentType.parse(mimeType)
            }

        updateProfile(ProfileUpdate(avatarPath = path))
        return path
    }

    override fun avatarUrl(path: String): String =
        supabase.storage.from("avatars").publicUrl(path)

    private fun getMyAuthUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: error("User not authenticated")
    }
}
