package dev.tireless.abun.finance

fun List<AccountWithBalance>.accountLookup(): Map<Long, AccountWithBalance> =
  associateBy { it.id }

fun AccountWithBalance.resolveRootId(accountLookup: Map<Long, AccountWithBalance>): Long {
  val visited = mutableSetOf<Long>()
  var current = this
  while (current.parentId != null && visited.add(current.id)) {
    val parentId = current.parentId ?: break
    val parent = accountLookup[parentId] ?: break
    current = parent
  }
  return current.id
}

fun AccountWithBalance.resolveAccountType(accountLookup: Map<Long, AccountWithBalance>): AccountType? =
  AccountType.fromRootId(resolveRootId(accountLookup))

fun List<AccountWithBalance>.leafAccountsForTypes(vararg types: AccountType): List<AccountWithBalance> {
  val typeSet = types.toSet()
  val lookup = accountLookup()
  val children = groupBy { it.parentId }
  return this
    .asSequence()
    .filter { account ->
      val type = account.resolveAccountType(lookup)
      val hasChildren = !children[account.id].isNullOrEmpty()
      type != null && type in typeSet && !hasChildren
    }
    .sortedBy { it.name }
    .toList()
}

fun AccountWithBalance.hierarchyPath(accountLookup: Map<Long, AccountWithBalance>): String {
  val segments = mutableListOf(name)
  val visited = mutableSetOf<Long>()
  var parentId = parentId
  while (parentId != null && visited.add(parentId)) {
    val parent = accountLookup[parentId] ?: break
    segments += parent.name
    parentId = parent.parentId
  }
  return segments.asReversed().joinToString(" / ")
}
