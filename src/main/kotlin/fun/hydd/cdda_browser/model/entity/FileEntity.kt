package `fun`.hydd.cdda_browser.model.entity

import javax.persistence.*

@Entity
@Table(name = "file_entity")
open class FileEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Column(name = "hash_code", nullable = false, unique = true, length = 256)
  open var hashCode: String? = null

//  @Lob
  @Column(name = "buffer", nullable = false)
  open var buffer: Array<Byte>? = null
}
