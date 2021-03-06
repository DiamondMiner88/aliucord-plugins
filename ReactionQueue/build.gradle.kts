version = "1.1.0"
description = "Queue up reactions to avoid hitting the ratelimit."

aliucord {
	changelog.set(
		"""
			# 1.1.0
			* Optionally close message menu after adding a reaction

			# 1.0.2
			* Fix message actions reacting not closing dialog

			# 1.0.1
			* Improve reflection
			* Use single threaded executor as queue

			# 1.0.0
			* Released
		""".trimIndent()
	)
}
