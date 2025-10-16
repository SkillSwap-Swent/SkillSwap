// AI-Generated: Offer class implementing Post interface for OFFER post type
package com.swent.skillswap.model.offer

import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.EveryTag

/**
 * Represents an offer post where a user is offering a skill or service. This class implements the
 * Post interface and always returns PostType.OFFER. It includes both the new Post interface
 * parameters and the legacy offer screen parameters for backward compatibility.
 *
 * @property give The skill, service, or item the author is offering (legacy parameter)
 * @property receive The skill, service, or item the author wants in return (legacy parameter)
 * @property authorID The unique identifier of the user who created the offer (legacy parameter)
 * @property thumbnail Optional thumbnail image ID for display purposes (legacy parameter)
 * @property uid A unique identifier for the offer post (Post interface)
 * @property title The title of the offer (Post interface)
 * @property description A detailed description of the skill being offered (Post interface)
 * @property ownerId The ID of the user who created the offer (Post interface)
 * @property tags A list of tags that categorize the offer (Post interface)
 * @property paymentMethods A list of accepted payment methods for the skill exchange (Post
 *   interface)
 * @property expiry The expiration timestamp of the offer (Post interface)
 * @property creation The timestamp when the offer was created (Post interface)
 * @property status The current status of the offer (Post interface)
 * @property media A list of media files associated with the offer (Post interface)
 */
data class Offer(
    val give: String = "",
    val receive: String = "",
    val authorID: String = "",
    val thumbnail: String = "",
    override val uid: String = "",
    override val title: String = "",
    override val description: String = "",
    override val ownerId: String = "",
    override val tags: List<EveryTag> = emptyList(),
    override val paymentMethods: List<PaymentMethod> = emptyList(),
    override val expiry: Timestamp = Timestamp.now(),
    override val creation: Timestamp = Timestamp.now(),
    override val status: PostStatus = PostStatus.POSTED,
    override val media: List<String> = emptyList(),
    override val type: PostType = PostType.OFFER
) : Post {

    // TODO: implement proper validation logic
    // https://github.com/orgs/SkillSwap-Swent/projects/1/views/2?filterQuery=&pane=issue&itemId=132697400
}
