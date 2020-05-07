/*
   +----------------------------------------------------------------------+
   | Zend Engine                                                          |
   +----------------------------------------------------------------------+
   | Copyright (c) 1998-2015 Zend Technologies Ltd. (http://www.zend.com) |
   +----------------------------------------------------------------------+
   | This source file is subject to version 2.00 of the Zend license,     |
   | that is bundled with this package in the file LICENSE, and is        |
   | available through the world-wide-web at the following url:           |
   | http://www.zend.com/license/2_00.txt.                                |
   | If you did not receive a copy of the Zend license and are unable to  |
   | obtain it through the world-wide-web, please send a note to          |
   | license@zend.com so we can mail you a copy immediately.              |
   +----------------------------------------------------------------------+
   | Authors: Andi Gutmans <andi@zend.com>                                |
   |          Zeev Suraski <zeev@zend.com>                                |
   +----------------------------------------------------------------------+
*/

/* $Id$ */

#include "zend_hash.h"
/* #include "zend_globals.h" */

#include <assert.h>
#include <stdarg.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <sys/stat.h>


int foo() {return 100;}

#define HANDLE_BLOCK_INTERRUPTIONS foo
#define HANDLE_UNBLOCK_INTERRUPTIONS foo
#define IS_INTERNED(s) (0)

#if (defined (__GNUC__) && __GNUC__ > 2 ) && !defined(DARWIN) && !defined(__hpux) && !defined(_AIX)
# define EXPECTED(condition)   __builtin_expect(condition, 1)
# define UNEXPECTED(condition) __builtin_expect(condition, 0)
#else
# define EXPECTED(condition)   (condition)
# define UNEXPECTED(condition) (condition)
#endif

#define ZEND_ASSERT(c)                  assert(c)


#define CONNECT_TO_BUCKET_DLLIST(element, list_head)		\
	(element)->pNext = (list_head);							\
	(element)->pLast = NULL;								\
	if ((element)->pNext) {									\
		(element)->pNext->pLast = (element);				\
	}

#define CONNECT_TO_GLOBAL_DLLIST_EX(element, ht, last, next)\
	(element)->pListLast = (last);							\
	(element)->pListNext = (next);							\
	if ((last) != NULL) {									\
		(last)->pListNext = (element);						\
	} else {												\
		(ht)->pListHead = (element);						\
	}														\
	if ((next) != NULL) {									\
		(next)->pListLast = (element);						\
	} else {												\
		(ht)->pListTail = (element);						\
	}														\

#define CONNECT_TO_GLOBAL_DLLIST(element, ht)									\
	CONNECT_TO_GLOBAL_DLLIST_EX(element, ht, (ht)->pListTail, (Bucket *) NULL);	\
	if ((ht)->pInternalPointer == NULL) {										\
		(ht)->pInternalPointer = (element);										\
	}


#define IS_CONSISTENT(a)
#define SET_INCONSISTENT(n)


#define HASH_PROTECT_RECURSION(ht)														\
	if ((ht)->bApplyProtection) {														\
		if ((ht)->nApplyCount++ >= 3) {													\
			perror("foobar");\
            exit(1);		\
		}																				\
	}


#define HASH_UNPROTECT_RECURSION(ht)													\
	if ((ht)->bApplyProtection) {														\
		(ht)->nApplyCount--;															\
	}


#define ZEND_HASH_IF_FULL_DO_RESIZE(ht)				\
	if ((ht)->nNumOfElements > (ht)->nTableSize) {	\
		zend_hash_do_resize(ht);					\
	}

static void zend_hash_do_resize(HashTable *ht);

 ulong zend_hash_func(const char *arKey, uint nKeyLength)
{
	return zend_inline_hash_func(arKey, nKeyLength);
}


#define UPDATE_DATA(ht, p, pData, nDataSize)											\
	if (nDataSize == sizeof(void*)) {													\
		if ((p)->pData != &(p)->pDataPtr) {												\
			free((p)->pData);									\
		}																				\
		memcpy(&(p)->pDataPtr, pData, sizeof(void *));									\
		(p)->pData = &(p)->pDataPtr;													\
	} else {																			\
		if ((p)->pData == &(p)->pDataPtr) {												\
			(p)->pData = (void *) malloc(nDataSize);			\
			(p)->pDataPtr=NULL;															\
		} else {																		\
			(p)->pData = (void *) realloc((p)->pData, nDataSize);	\
			/* (p)->pDataPtr is already NULL so no need to initi,alize it */				\
		}																				\
		memcpy((p)->pData, pData, nDataSize);											\
	}

#define INIT_DATA(ht, p, _pData, nDataSize);								\
	if (nDataSize == sizeof(void*)) {									\
		memcpy(&(p)->pDataPtr, (_pData), sizeof(void *));					\
		(p)->pData = &(p)->pDataPtr;									\
	} else {															\
		(p)->pData = (void *) malloc(nDataSize);\
		memcpy((p)->pData, (_pData), nDataSize);							\
		(p)->pDataPtr=NULL;												\
	}

#define CHECK_INIT(ht) do {												\
	if (UNEXPECTED((ht)->nTableMask == 0)) {								\
		(ht)->arBuckets = (Bucket **) calloc((ht)->nTableSize, sizeof(Bucket *));	\
		(ht)->nTableMask = (ht)->nTableSize - 1;						\
	}																	\
} while (0)

static const Bucket *uninitialized_bucket = NULL;

static void i_zend_hash_bucket_delete(HashTable *ht, Bucket *p)
{
#ifdef ZEND_SIGNALS
	TSRMLS_FETCH();
#endif

	HANDLE_BLOCK_INTERRUPTIONS();
	if (p->pLast) {
		p->pLast->pNext = p->pNext;
	} else {
		ht->arBuckets[p->h & ht->nTableMask] = p->pNext;
	}
	if (p->pNext) {
		p->pNext->pLast = p->pLast;
	}
	if (p->pListLast != NULL) {
		p->pListLast->pListNext = p->pListNext;
	} else {
		/* Deleting the head of the list */
		ht->pListHead = p->pListNext;
	}
	if (p->pListNext != NULL) {
		p->pListNext->pListLast = p->pListLast;
	} else {
		/* Deleting the tail of the list */
		ht->pListTail = p->pListLast;
	}
	if (ht->pInternalPointer == p) {
		ht->pInternalPointer = p->pListNext;
	}
	ht->nNumOfElements--;
	if (ht->pDestructor) {
		ht->pDestructor(p->pData);
	}
	if (p->pData != &p->pDataPtr) {
		free(p->pData);
	}
	free(p);
	HANDLE_UNBLOCK_INTERRUPTIONS();
}

static void zend_hash_bucket_delete(HashTable *ht, Bucket *p) {
	i_zend_hash_bucket_delete(ht, p);
}

 int _zend_hash_init(HashTable *ht, uint nSize, dtor_func_t pDestructor, zend_bool persistent )
{
	uint i = 3;

	SET_INCONSISTENT(HT_OK);

	if (nSize >= 0x80000000) {
		/* prevent overflow */
		ht->nTableSize = 0x80000000;
	} else {
		while ((1U << i) < nSize) {
			i++;
		}
		ht->nTableSize = 1 << i;
	}

	ht->nTableMask = 0;	/* 0 means that ht->arBuckets is uninitialized */
	ht->pDestructor = pDestructor;
	ht->arBuckets = (Bucket**)&uninitialized_bucket;
	ht->pListHead = NULL;
	ht->pListTail = NULL;
	ht->nNumOfElements = 0;
	ht->nNextFreeElement = 0;
	ht->pInternalPointer = NULL;
	ht->persistent = persistent;
	ht->nApplyCount = 0;
	ht->bApplyProtection = 1;
	return SUCCESS;
}


 int _zend_hash_init_ex(HashTable *ht, uint nSize, dtor_func_t pDestructor, zend_bool persistent, zend_bool bApplyProtection )
{
	int retval = _zend_hash_init(ht, nSize, pDestructor, persistent );

	ht->bApplyProtection = bApplyProtection;
	return retval;
}


 void zend_hash_set_apply_protection(HashTable *ht, zend_bool bApplyProtection)
{
	ht->bApplyProtection = bApplyProtection;
}



 int _zend_hash_add_or_update(HashTable *ht, const char *arKey, uint nKeyLength, void *pData, uint nDataSize, void **pDest, int flag )
{
	ulong h;
	uint nIndex;
	Bucket *p;
#ifdef ZEND_SIGNALS
	TSRMLS_FETCH();
#endif

	IS_CONSISTENT(ht);

	ZEND_ASSERT(nKeyLength != 0);

	CHECK_INIT(ht);

	h = zend_inline_hash_func(arKey, nKeyLength);
	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if (p->arKey == arKey ||
			((p->h == h) && (p->nKeyLength == nKeyLength) && !memcmp(p->arKey, arKey, nKeyLength))) {
				if (flag & HASH_ADD) {
					return FAILURE;
				}
				ZEND_ASSERT(p->pData != pData);
				HANDLE_BLOCK_INTERRUPTIONS();
				if (ht->pDestructor) {
					ht->pDestructor(p->pData);
				}
				UPDATE_DATA(ht, p, pData, nDataSize);
				if (pDest) {
					*pDest = p->pData;
				}
				HANDLE_UNBLOCK_INTERRUPTIONS();
				return SUCCESS;
		}
		p = p->pNext;
	}

	if (IS_INTERNED(arKey)) {
		p = (Bucket *) malloc(sizeof(Bucket));
		p->arKey = arKey;
	} else {
		p = (Bucket *) malloc(sizeof(Bucket) + nKeyLength);
		p->arKey = (const char*)(p + 1);
		memcpy((char*)p->arKey, arKey, nKeyLength);
	}
	p->nKeyLength = nKeyLength;
	INIT_DATA(ht, p, pData, nDataSize);
	p->h = h;
	CONNECT_TO_BUCKET_DLLIST(p, ht->arBuckets[nIndex]);
	if (pDest) {
		*pDest = p->pData;
	}

	HANDLE_BLOCK_INTERRUPTIONS();
	CONNECT_TO_GLOBAL_DLLIST(p, ht);
	ht->arBuckets[nIndex] = p;
	HANDLE_UNBLOCK_INTERRUPTIONS();

	ht->nNumOfElements++;
	ZEND_HASH_IF_FULL_DO_RESIZE(ht);		/* If the Hash table is full, resize it */
	return SUCCESS;
}

 int _zend_hash_quick_add_or_update(HashTable *ht, const char *arKey, uint nKeyLength, ulong h, void *pData, uint nDataSize, void **pDest, int flag )
{
	uint nIndex;
	Bucket *p;
#ifdef ZEND_SIGNALS
	TSRMLS_FETCH();
#endif

	IS_CONSISTENT(ht);

	ZEND_ASSERT(nKeyLength != 0);

	CHECK_INIT(ht);
	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if (p->arKey == arKey ||
			((p->h == h) && (p->nKeyLength == nKeyLength) && !memcmp(p->arKey, arKey, nKeyLength))) {
				if (flag & HASH_ADD) {
					return FAILURE;
				}
				ZEND_ASSERT(p->pData != pData);
				HANDLE_BLOCK_INTERRUPTIONS();
				if (ht->pDestructor) {
					ht->pDestructor(p->pData);
				}
				UPDATE_DATA(ht, p, pData, nDataSize);
				if (pDest) {
					*pDest = p->pData;
				}
				HANDLE_UNBLOCK_INTERRUPTIONS();
				return SUCCESS;
		}
		p = p->pNext;
	}

	if (IS_INTERNED(arKey)) {
		p = (Bucket *) malloc(sizeof(Bucket));
		p->arKey = arKey;
	} else {
		p = (Bucket *) malloc(sizeof(Bucket) + nKeyLength);
		p->arKey = (const char*)(p + 1);
		memcpy((char*)p->arKey, arKey, nKeyLength);
	}

	p->nKeyLength = nKeyLength;
	INIT_DATA(ht, p, pData, nDataSize);
	p->h = h;

	CONNECT_TO_BUCKET_DLLIST(p, ht->arBuckets[nIndex]);

	if (pDest) {
		*pDest = p->pData;
	}

	HANDLE_BLOCK_INTERRUPTIONS();
	ht->arBuckets[nIndex] = p;
	CONNECT_TO_GLOBAL_DLLIST(p, ht);
	HANDLE_UNBLOCK_INTERRUPTIONS();

	ht->nNumOfElements++;
	ZEND_HASH_IF_FULL_DO_RESIZE(ht);		/* If the Hash table is full, resize it */
	return SUCCESS;
}


 int zend_hash_add_empty_element(HashTable *ht, const char *arKey, uint nKeyLength)
{
	void *dummy = (void *) 1;

	return zend_hash_add(ht, arKey, nKeyLength, &dummy, sizeof(void *), NULL);
}


 int _zend_hash_index_update_or_next_insert(HashTable *ht, ulong h, void *pData, uint nDataSize, void **pDest, int flag )
{
	uint nIndex;
	Bucket *p;
#ifdef ZEND_SIGNALS
	TSRMLS_FETCH();
#endif

	IS_CONSISTENT(ht);
	CHECK_INIT(ht);

	if (flag & HASH_NEXT_INSERT) {
		h = ht->nNextFreeElement;
	}
	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if ((p->nKeyLength == 0) && (p->h == h)) {
			if (flag & HASH_NEXT_INSERT || flag & HASH_ADD) {
				return FAILURE;
			}
			ZEND_ASSERT(p->pData != pData);
			HANDLE_BLOCK_INTERRUPTIONS();
			if (ht->pDestructor) {
				ht->pDestructor(p->pData);
			}
			UPDATE_DATA(ht, p, pData, nDataSize);
			HANDLE_UNBLOCK_INTERRUPTIONS();
			if (pDest) {
				*pDest = p->pData;
			}
			return SUCCESS;
		}
		p = p->pNext;
	}
	p = (Bucket *) malloc(sizeof(Bucket));
	p->arKey = NULL;
	p->nKeyLength = 0; /* Numeric indices are marked by making the nKeyLength == 0 */
	p->h = h;
	INIT_DATA(ht, p, pData, nDataSize);
	if (pDest) {
		*pDest = p->pData;
	}

	CONNECT_TO_BUCKET_DLLIST(p, ht->arBuckets[nIndex]);

	HANDLE_BLOCK_INTERRUPTIONS();
	ht->arBuckets[nIndex] = p;
	CONNECT_TO_GLOBAL_DLLIST(p, ht);
	HANDLE_UNBLOCK_INTERRUPTIONS();

	if ((long)h >= (long)ht->nNextFreeElement) {
		ht->nNextFreeElement = h < LONG_MAX ? h + 1 : LONG_MAX;
	}
	ht->nNumOfElements++;
	ZEND_HASH_IF_FULL_DO_RESIZE(ht);
	return SUCCESS;
}


static void zend_hash_do_resize(HashTable *ht)
{
	Bucket **t;
#ifdef ZEND_SIGNALS
	TSRMLS_FETCH();
#endif

	IS_CONSISTENT(ht);

	if ((ht->nTableSize << 1) > 0) {	/* Let's double the table size */
		t = (Bucket **) realloc(ht->arBuckets, (ht->nTableSize << 1) * sizeof(Bucket *));
		HANDLE_BLOCK_INTERRUPTIONS();
		ht->arBuckets = t;
		ht->nTableSize = (ht->nTableSize << 1);
		ht->nTableMask = ht->nTableSize - 1;
		zend_hash_rehash(ht);
		HANDLE_UNBLOCK_INTERRUPTIONS();
	}
}

 int zend_hash_rehash(HashTable *ht)
{
	Bucket *p;
	uint nIndex;

	IS_CONSISTENT(ht);
	if (UNEXPECTED(ht->nNumOfElements == 0)) {
		return SUCCESS;
	}

	memset(ht->arBuckets, 0, ht->nTableSize * sizeof(Bucket *));
	for (p = ht->pListHead; p != NULL; p = p->pListNext) {
		nIndex = p->h & ht->nTableMask;
		CONNECT_TO_BUCKET_DLLIST(p, ht->arBuckets[nIndex]);
		ht->arBuckets[nIndex] = p;
	}
	return SUCCESS;
}

 void zend_hash_reindex(HashTable *ht, zend_bool only_integer_keys) {
	Bucket *p;
	uint nIndex;
	ulong offset = 0;

	IS_CONSISTENT(ht);
	if (UNEXPECTED(ht->nNumOfElements == 0)) {
		ht->nNextFreeElement = 0;
		return;
	}

	memset(ht->arBuckets, 0, ht->nTableSize * sizeof(Bucket *));
	for (p = ht->pListHead; p != NULL; p = p->pListNext) {
		if (!only_integer_keys || p->nKeyLength == 0) {
			p->h = offset++;
			p->nKeyLength = 0;
		}

		nIndex = p->h & ht->nTableMask;
		CONNECT_TO_BUCKET_DLLIST(p, ht->arBuckets[nIndex]);
		ht->arBuckets[nIndex] = p;
	}
	ht->nNextFreeElement = offset;
}

 int zend_hash_del_key_or_index(HashTable *ht, const char *arKey, uint nKeyLength, ulong h, int flag)
{
	uint nIndex;
	Bucket *p;

	IS_CONSISTENT(ht);

	if (flag == HASH_DEL_KEY) {
		h = zend_inline_hash_func(arKey, nKeyLength);
	}
	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if ((p->h == h)
			 && (p->nKeyLength == nKeyLength)
			 && ((p->nKeyLength == 0) /* Numeric index (short circuits the memcmp() check) */
				 || !memcmp(p->arKey, arKey, nKeyLength))) { /* String index */
			i_zend_hash_bucket_delete(ht, p);
			return SUCCESS;
		}
		p = p->pNext;
	}
	return FAILURE;
}


 void zend_hash_destroy(HashTable *ht)
{
	Bucket *p, *q;

	IS_CONSISTENT(ht);

	SET_INCONSISTENT(HT_IS_DESTROYING);

	p = ht->pListHead;
	while (p != NULL) {
		q = p;
		p = p->pListNext;
		if (ht->pDestructor) {
			ht->pDestructor(q->pData);
		}
		if (q->pData != &q->pDataPtr) {
			free(q->pData);
		}
		free(q);
	}
	if (ht->nTableMask) {
		free(ht->arBuckets);
	}

	SET_INCONSISTENT(HT_DESTROYED);
}


 void zend_hash_clean(HashTable *ht)
{
	Bucket *p, *q;

	IS_CONSISTENT(ht);

	p = ht->pListHead;

	if (ht->nTableMask) {
		memset(ht->arBuckets, 0, ht->nTableSize*sizeof(Bucket *));
	}
	ht->pListHead = NULL;
	ht->pListTail = NULL;
	ht->nNumOfElements = 0;
	ht->nNextFreeElement = 0;
	ht->pInternalPointer = NULL;

	while (p != NULL) {
		q = p;
		p = p->pListNext;
		if (ht->pDestructor) {
			ht->pDestructor(q->pData);
		}
		if (q->pData != &q->pDataPtr) {
			free(q->pData);
		}
		free(q);
	}
}

 void zend_hash_graceful_destroy(HashTable *ht)
{
	IS_CONSISTENT(ht);

	while (ht->pListHead != NULL) {
		zend_hash_bucket_delete(ht, ht->pListHead);
	}

	if (ht->nTableMask) {
		free(ht->arBuckets);
	}

	SET_INCONSISTENT(HT_DESTROYED);
}

 void zend_hash_graceful_reverse_destroy(HashTable *ht)
{
	IS_CONSISTENT(ht);

	while (ht->pListTail != NULL) {
		zend_hash_bucket_delete(ht, ht->pListTail);
	}

	if (ht->nTableMask) {
		free(ht->arBuckets);
	}

	SET_INCONSISTENT(HT_DESTROYED);
}

/* This is used to recurse elements and selectively delete certain entries
 * from a hashtable. apply_func() receives the data and decides if the entry
 * should be deleted or recursion should be stopped. The following three
 * return codes are possible:
 * ZEND_HASH_APPLY_KEEP   - continue
 * ZEND_HASH_APPLY_STOP   - stop iteration
 * ZEND_HASH_APPLY_REMOVE - delete the element, combineable with the former
 */

 void zend_hash_apply(HashTable *ht, apply_func_t apply_func )
{
	Bucket *p;

	IS_CONSISTENT(ht);

	HASH_PROTECT_RECURSION(ht);
	p = ht->pListHead;
	while (p != NULL) {
		int result = apply_func(p->pData );

		Bucket *p_next = p->pListNext;
		if (result & ZEND_HASH_APPLY_REMOVE) {
			zend_hash_bucket_delete(ht, p);
		}
		p = p_next;

		if (result & ZEND_HASH_APPLY_STOP) {
			break;
		}
	}
	HASH_UNPROTECT_RECURSION(ht);
}


 void zend_hash_apply_with_argument(HashTable *ht, apply_func_arg_t apply_func, void *argument )
{
	Bucket *p;

	IS_CONSISTENT(ht);

	HASH_PROTECT_RECURSION(ht);
	p = ht->pListHead;
	while (p != NULL) {
		int result = apply_func(p->pData, argument );

		Bucket *p_next = p->pListNext;
		if (result & ZEND_HASH_APPLY_REMOVE) {
			zend_hash_bucket_delete(ht, p);
		}
		p = p_next;

		if (result & ZEND_HASH_APPLY_STOP) {
			break;
		}
	}
	HASH_UNPROTECT_RECURSION(ht);
}


 void zend_hash_apply_with_arguments(HashTable *ht , apply_func_args_t apply_func, int num_args, ...)
{
	Bucket *p;
	va_list args;
	zend_hash_key hash_key;

	IS_CONSISTENT(ht);

	HASH_PROTECT_RECURSION(ht);

	p = ht->pListHead;
	while (p != NULL) {
		int result;
		Bucket *p_next;

		va_start(args, num_args);
		hash_key.arKey = p->arKey;
		hash_key.nKeyLength = p->nKeyLength;
		hash_key.h = p->h;
		result = apply_func(p->pData , num_args, args, &hash_key);

		p_next = p->pListNext;
		if (result & ZEND_HASH_APPLY_REMOVE) {
			zend_hash_bucket_delete(ht, p);
		}
		p = p_next;

		if (result & ZEND_HASH_APPLY_STOP) {
			va_end(args);
			break;
		}
		va_end(args);
	}

	HASH_UNPROTECT_RECURSION(ht);
}


 void zend_hash_reverse_apply(HashTable *ht, apply_func_t apply_func )
{
	Bucket *p;

	IS_CONSISTENT(ht);

	HASH_PROTECT_RECURSION(ht);
	p = ht->pListTail;
	while (p != NULL) {
		int result = apply_func(p->pData );

		Bucket *p_last = p->pListLast;
		if (result & ZEND_HASH_APPLY_REMOVE) {
			zend_hash_bucket_delete(ht, p);
		}
		p = p_last;

		if (result & ZEND_HASH_APPLY_STOP) {
			break;
		}
	}
	HASH_UNPROTECT_RECURSION(ht);
}


 void zend_hash_copy(HashTable *target, HashTable *source, copy_ctor_func_t pCopyConstructor, void *tmp, uint size)
{
	Bucket *p;
	void *new_entry;
	zend_bool setTargetPointer;

	IS_CONSISTENT(source);
	IS_CONSISTENT(target);

	setTargetPointer = !target->pInternalPointer;
	p = source->pListHead;
	while (p) {
		if (setTargetPointer && source->pInternalPointer == p) {
			target->pInternalPointer = NULL;
		}
		if (p->nKeyLength) {
			zend_hash_quick_update(target, p->arKey, p->nKeyLength, p->h, p->pData, size, &new_entry);
		} else {
			zend_hash_index_update(target, p->h, p->pData, size, &new_entry);
		}
		if (pCopyConstructor) {
			pCopyConstructor(new_entry);
		}
		p = p->pListNext;
	}
	if (!target->pInternalPointer) {
		target->pInternalPointer = target->pListHead;
	}
}


 void _zend_hash_merge(HashTable *target, HashTable *source, copy_ctor_func_t pCopyConstructor, void *tmp, uint size, int overwrite )
{
	Bucket *p;
	void *t;
	int mode = (overwrite?HASH_UPDATE:HASH_ADD);

	IS_CONSISTENT(source);
	IS_CONSISTENT(target);

	p = source->pListHead;
	while (p) {
		if (p->nKeyLength>0) {
			if (_zend_hash_quick_add_or_update(target, p->arKey, p->nKeyLength, p->h, p->pData, size, &t, mode )==SUCCESS && pCopyConstructor) {
				pCopyConstructor(t);
			}
		} else {
			if ((mode==HASH_UPDATE || !zend_hash_index_exists(target, p->h)) && zend_hash_index_update(target, p->h, p->pData, size, &t)==SUCCESS && pCopyConstructor) {
				pCopyConstructor(t);
			}
		}
		p = p->pListNext;
	}
	target->pInternalPointer = target->pListHead;
}


static zend_bool zend_hash_replace_checker_wrapper(HashTable *target, void *source_data, Bucket *p, void *pParam, merge_checker_func_t merge_checker_func)
{
	zend_hash_key hash_key;

	hash_key.arKey = p->arKey;
	hash_key.nKeyLength = p->nKeyLength;
	hash_key.h = p->h;
	return merge_checker_func(target, source_data, &hash_key, pParam);
}


 void zend_hash_merge_ex(HashTable *target, HashTable *source, copy_ctor_func_t pCopyConstructor, uint size, merge_checker_func_t pMergeSource, void *pParam)
{
	Bucket *p;
	void *t;

	IS_CONSISTENT(source);
	IS_CONSISTENT(target);

	p = source->pListHead;
	while (p) {
		if (zend_hash_replace_checker_wrapper(target, p->pData, p, pParam, pMergeSource)) {
			if (zend_hash_quick_update(target, p->arKey, p->nKeyLength, p->h, p->pData, size, &t)==SUCCESS && pCopyConstructor) {
				pCopyConstructor(t);
			}
		}
		p = p->pListNext;
	}
	target->pInternalPointer = target->pListHead;
}


/* Returns SUCCESS if found and FAILURE if not. The pointer to the
 * data is returned in pData. The reason is that there's no reason
 * someone using the hash table might not want to have NULL data
 */
 int zend_hash_find(const HashTable *ht, const char *arKey, uint nKeyLength, void **pData)
{
	ulong h;
	uint nIndex;
	Bucket *p;

	IS_CONSISTENT(ht);

	h = zend_inline_hash_func(arKey, nKeyLength);
	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if (p->arKey == arKey ||
			((p->h == h) && (p->nKeyLength == nKeyLength) && !memcmp(p->arKey, arKey, nKeyLength))) {
				*pData = p->pData;
				return SUCCESS;
		}
		p = p->pNext;
	}
	return FAILURE;
}


 int zend_hash_quick_find(const HashTable *ht, const char *arKey, uint nKeyLength, ulong h, void **pData)
{
	uint nIndex;
	Bucket *p;

	ZEND_ASSERT(nKeyLength != 0);

	IS_CONSISTENT(ht);

	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if (p->arKey == arKey ||
			((p->h == h) && (p->nKeyLength == nKeyLength) && !memcmp(p->arKey, arKey, nKeyLength))) {
				*pData = p->pData;
				return SUCCESS;
		}
		p = p->pNext;
	}
	return FAILURE;
}


 int zend_hash_exists(const HashTable *ht, const char *arKey, uint nKeyLength)
{
	ulong h;
	uint nIndex;
	Bucket *p;

	IS_CONSISTENT(ht);

	h = zend_inline_hash_func(arKey, nKeyLength);
	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if (p->arKey == arKey ||
			((p->h == h) && (p->nKeyLength == nKeyLength) && !memcmp(p->arKey, arKey, nKeyLength))) {
				return 1;
		}
		p = p->pNext;
	}
	return 0;
}


 int zend_hash_quick_exists(const HashTable *ht, const char *arKey, uint nKeyLength, ulong h)
{
	uint nIndex;
	Bucket *p;

	ZEND_ASSERT(nKeyLength != 0);

	IS_CONSISTENT(ht);

	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if (p->arKey == arKey ||
			((p->h == h) && (p->nKeyLength == nKeyLength) && !memcmp(p->arKey, arKey, nKeyLength))) {
				return 1;
		}
		p = p->pNext;
	}
	return 0;

}


 int zend_hash_index_find(const HashTable *ht, ulong h, void **pData)
{
	uint nIndex;
	Bucket *p;

	IS_CONSISTENT(ht);

	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if ((p->h == h) && (p->nKeyLength == 0)) {
			*pData = p->pData;
			return SUCCESS;
		}
		p = p->pNext;
	}
	return FAILURE;
}


 int zend_hash_index_exists(const HashTable *ht, ulong h)
{
	uint nIndex;
	Bucket *p;

	IS_CONSISTENT(ht);

	nIndex = h & ht->nTableMask;

	p = ht->arBuckets[nIndex];
	while (p != NULL) {
		if ((p->h == h) && (p->nKeyLength == 0)) {
			return 1;
		}
		p = p->pNext;
	}
	return 0;
}


 int zend_hash_num_elements(const HashTable *ht)
{
	IS_CONSISTENT(ht);

	return ht->nNumOfElements;
}


 int zend_hash_get_pointer(const HashTable *ht, HashPointer *ptr)
{
	ptr->pos = ht->pInternalPointer;
	if (ht->pInternalPointer) {
		ptr->h = ht->pInternalPointer->h;
		return 1;
	} else {
		ptr->h = 0;
		return 0;
	}
}

 int zend_hash_set_pointer(HashTable *ht, const HashPointer *ptr)
{
	if (ptr->pos == NULL) {
		ht->pInternalPointer = NULL;
	} else if (ht->pInternalPointer != ptr->pos) {
		Bucket *p;

		IS_CONSISTENT(ht);
		p = ht->arBuckets[ptr->h & ht->nTableMask];
		while (p != NULL) {
			if (p == ptr->pos) {
				ht->pInternalPointer = p;
				return 1;
			}
			p = p->pNext;
		}
		return 0;
	}
	return 1;
}

 void zend_hash_internal_pointer_reset_ex(HashTable *ht, HashPosition *pos)
{
	IS_CONSISTENT(ht);

	if (pos)
		*pos = ht->pListHead;
	else
		ht->pInternalPointer = ht->pListHead;
}


/* This function will be extremely optimized by remembering
 * the end of the list
 */
 void zend_hash_internal_pointer_end_ex(HashTable *ht, HashPosition *pos)
{
	IS_CONSISTENT(ht);

	if (pos)
		*pos = ht->pListTail;
	else
		ht->pInternalPointer = ht->pListTail;
}


 int zend_hash_move_forward_ex(HashTable *ht, HashPosition *pos)
{
	HashPosition *current = pos ? pos : &ht->pInternalPointer;

	IS_CONSISTENT(ht);

	if (*current) {
		*current = (*current)->pListNext;
		return SUCCESS;
	} else
		return FAILURE;
}

 int zend_hash_move_backwards_ex(HashTable *ht, HashPosition *pos)
{
	HashPosition *current = pos ? pos : &ht->pInternalPointer;

	IS_CONSISTENT(ht);

	if (*current) {
		*current = (*current)->pListLast;
		return SUCCESS;
	} else
		return FAILURE;
}


/* This function should be made binary safe  */
 int zend_hash_get_current_key_ex(const HashTable *ht, char **str_index, uint *str_length, ulong *num_index, zend_bool duplicate, HashPosition *pos)
{
	Bucket *p;

	p = pos ? (*pos) : ht->pInternalPointer;

	IS_CONSISTENT(ht);

	if (p) {
		if (p->nKeyLength) {
			if (duplicate) {
				*str_index = strndup(p->arKey, p->nKeyLength - 1);
			} else {
				*str_index = (char*)p->arKey;
			}
			if (str_length) {
				*str_length = p->nKeyLength;
			}
			return HASH_KEY_IS_STRING;
		} else {
			*num_index = p->h;
			return HASH_KEY_IS_LONG;
		}
	}
	return HASH_KEY_NON_EXISTENT;
}

 /* void zend_hash_get_current_key_zval_ex(const HashTable *ht, zval *key, HashPosition *pos) { */
	/* Bucket *p; */

	/* IS_CONSISTENT(ht); */

	/* p = pos ? (*pos) : ht->pInternalPointer; */

	/* if (!p) { */
		/* Z_TYPE_P(key) = IS_NULL; */
	/* } else if (p->nKeyLength) { */
		/* Z_TYPE_P(key) = IS_STRING; */
		/* Z_STRVAL_P(key) = IS_INTERNED(p->arKey) ? (char*)p->arKey : estrndup(p->arKey, p->nKeyLength - 1); */
		/* Z_STRLEN_P(key) = p->nKeyLength - 1; */
	/* } else { */
		/* Z_TYPE_P(key) = IS_LONG; */
		/* Z_LVAL_P(key) = p->h; */
	/* } */
/* } */

 int zend_hash_get_current_key_type_ex(HashTable *ht, HashPosition *pos)
{
	Bucket *p;

	p = pos ? (*pos) : ht->pInternalPointer;

	IS_CONSISTENT(ht);

	if (p) {
		if (p->nKeyLength) {
			return HASH_KEY_IS_STRING;
		} else {
			return HASH_KEY_IS_LONG;
		}
	}
	return HASH_KEY_NON_EXISTENT;
}


 int zend_hash_get_current_data_ex(HashTable *ht, void **pData, HashPosition *pos)
{
	Bucket *p;

	p = pos ? (*pos) : ht->pInternalPointer;

	IS_CONSISTENT(ht);

	if (p) {
		*pData = p->pData;
		return SUCCESS;
	} else {
		return FAILURE;
	}
}

/* This function changes key of current element without changing elements'
 * order. If element with target key already exists, it will be deleted first.
 */
 int zend_hash_update_current_key_ex(HashTable *ht, int key_type, const char *str_index, uint str_length, ulong num_index, int mode, HashPosition *pos)
{
	Bucket *p, *q;
	ulong h;
#ifdef ZEND_SIGNALS
	TSRMLS_FETCH();
#endif

	p = pos ? (*pos) : ht->pInternalPointer;

	IS_CONSISTENT(ht);

	if (p) {
		if (key_type == HASH_KEY_IS_LONG) {
			str_length = 0;
			if (!p->nKeyLength && p->h == num_index) {
				return SUCCESS;
			}

			q = ht->arBuckets[num_index & ht->nTableMask];
			while (q != NULL) {
				if (!q->nKeyLength && q->h == num_index) {
					break;
				}
				q = q->pNext;
			}
		} else if (key_type == HASH_KEY_IS_STRING) {
			if (IS_INTERNED(str_index)) {
				h = INTERNED_HASH(str_index);
			} else {
				h = zend_inline_hash_func(str_index, str_length);
			}

			if (p->arKey == str_index ||
			    (p->nKeyLength == str_length &&
			     p->h == h &&
			     memcmp(p->arKey, str_index, str_length) == 0)) {
				return SUCCESS;
			}

			q = ht->arBuckets[h & ht->nTableMask];

			while (q != NULL) {
				if (q->arKey == str_index ||
				    (q->h == h && q->nKeyLength == str_length &&
				     memcmp(q->arKey, str_index, str_length) == 0)) {
					break;
				}
				q = q->pNext;
			}
		} else {
			return FAILURE;
		}

		if (q) {
			if (mode != HASH_UPDATE_KEY_ANYWAY) {
				Bucket *r = p->pListLast;
				int found = HASH_UPDATE_KEY_IF_BEFORE;

				while (r) {
					if (r == q) {
						found = HASH_UPDATE_KEY_IF_AFTER;
						break;
					}
					r = r->pListLast;
				}
				if (mode & found) {
					/* delete current bucket */
					zend_hash_bucket_delete(ht, p);
					return FAILURE;
				}
			}

			/* delete another bucket with the same key */
			zend_hash_bucket_delete(ht, q);
		}

		HANDLE_BLOCK_INTERRUPTIONS();

		if (p->pNext) {
			p->pNext->pLast = p->pLast;
		}
		if (p->pLast) {
			p->pLast->pNext = p->pNext;
		} else {
			ht->arBuckets[p->h & ht->nTableMask] = p->pNext;
		}

		if ((IS_INTERNED(p->arKey) != IS_INTERNED(str_index)) ||
		    (!IS_INTERNED(p->arKey) && p->nKeyLength != str_length)) {
			Bucket *q;

			if (IS_INTERNED(str_index)) {
				q = (Bucket *) malloc(sizeof(Bucket));
			} else {
				q = (Bucket *) malloc(sizeof(Bucket) + str_length);
			}

			q->nKeyLength = str_length;
			if (p->pData == &p->pDataPtr) {
				q->pData = &q->pDataPtr;
			} else {
				q->pData = p->pData;
			}
			q->pDataPtr = p->pDataPtr;

			CONNECT_TO_GLOBAL_DLLIST_EX(q, ht, p->pListLast, p->pListNext);
			if (ht->pInternalPointer == p) {
				ht->pInternalPointer = q;
			}

			if (pos) {
				*pos = q;
			}
			free(p);
			p = q;
		}

		if (key_type == HASH_KEY_IS_LONG) {
			p->h = num_index;
			if ((long)num_index >= (long)ht->nNextFreeElement) {
				ht->nNextFreeElement = num_index < LONG_MAX ? num_index + 1 : LONG_MAX;
			}
		} else {
			p->h = h;
			p->nKeyLength = str_length;
			if (IS_INTERNED(str_index)) {
				p->arKey = str_index;
			} else {
				p->arKey = (const char*)(p+1);
				memcpy((char*)p->arKey, str_index, str_length);
			}
		}

		CONNECT_TO_BUCKET_DLLIST(p, ht->arBuckets[p->h & ht->nTableMask]);
		ht->arBuckets[p->h & ht->nTableMask] = p;
		HANDLE_UNBLOCK_INTERRUPTIONS();

		return SUCCESS;
	} else {
		return FAILURE;
	}
}

/* Performs an in-place splice operation on a hashtable:
 * The elements between offset and offset+length are removed and the elements in list[list_count]
 * are inserted in their place. The removed elements can be optionally collected into a hashtable.
 * This operation reindexes the hashtable, i.e. integer keys will be zero-based and sequential,
 * while string keys stay intact. The same applies to the elements inserted into the removed HT. */
 void _zend_hash_splice(HashTable *ht, uint nDataSize, copy_ctor_func_t pCopyConstructor, uint offset, uint length, void **list, uint list_count, HashTable *removed ) /* {{{ */
{
	int pos;
	Bucket *p;

	IS_CONSISTENT(ht);
	CHECK_INIT(ht);

	/* Skip all elements until offset */
	for (pos = 0, p = ht->pListHead; pos < offset && p; pos++, p = p->pListNext);

	while (pos < offset + length && p) {
		/* Copy removed element into HT, if it was specified */
		if (removed != NULL) {
			void *new_entry;

			if (p->nKeyLength == 0) {
				zend_hash_next_index_insert(removed, p->pData, sizeof(zval *), &new_entry);
			} else {
				zend_hash_quick_update(removed, p->arKey, p->nKeyLength, p->h, p->pData, sizeof(zval *), &new_entry);
			}

			if (pCopyConstructor) {
				pCopyConstructor(new_entry);
			}
		}

		/* Remove element */
		{
			Bucket *p_next = p->pListNext;
			zend_hash_bucket_delete(ht, p);
			p = p_next;
		}

		pos++;
	}

	if (list != NULL) {
		int i;
		for (i = 0; i < list_count; i++) {
			/* Add new element only to the global linked list, not the bucket list.
			 * Also use key 0 for everything, as we'll reindex the hashtable anyways. */
			Bucket *q = (Bucket *) malloc(sizeof(Bucket));
			q->arKey = NULL;
			q->nKeyLength = 0;
			q->h = 0;
			INIT_DATA(ht, q, list[i], nDataSize);

			CONNECT_TO_GLOBAL_DLLIST_EX(q, ht, p ? p->pListLast : ht->pListTail, p);

			ht->nNumOfElements++;

			if (pCopyConstructor) {
				pCopyConstructor(q->pData);
			}
		}

		ZEND_HASH_IF_FULL_DO_RESIZE(ht);
	}

	zend_hash_reindex(ht, 1);
}
/* }}} */

 int zend_hash_sort(HashTable *ht, sort_func_t sort_func,
							compare_func_t compar, int renumber )
{
	Bucket **arTmp;
	Bucket *p;
	int i, j;

	IS_CONSISTENT(ht);

	if (!(ht->nNumOfElements>1) && !(renumber && ht->nNumOfElements>0)) { /* Doesn't require sorting */
		return SUCCESS;
	}
	arTmp = (Bucket **) malloc(ht->nNumOfElements * sizeof(Bucket *));
	p = ht->pListHead;
	i = 0;
	while (p) {
		arTmp[i] = p;
		p = p->pListNext;
		i++;
	}

	(*sort_func)((void *) arTmp, i, sizeof(Bucket *), compar );

	HANDLE_BLOCK_INTERRUPTIONS();
	ht->pListHead = arTmp[0];
	ht->pListTail = NULL;
	ht->pInternalPointer = ht->pListHead;

	arTmp[0]->pListLast = NULL;
	if (i > 1) {
		arTmp[0]->pListNext = arTmp[1];
		for (j = 1; j < i-1; j++) {
			arTmp[j]->pListLast = arTmp[j-1];
			arTmp[j]->pListNext = arTmp[j+1];
		}
		arTmp[j]->pListLast = arTmp[j-1];
		arTmp[j]->pListNext = NULL;
	} else {
		arTmp[0]->pListNext = NULL;
	}
	ht->pListTail = arTmp[i-1];

	free(arTmp);
	HANDLE_UNBLOCK_INTERRUPTIONS();

	if (renumber) {
		zend_hash_reindex(ht, 0);
	}
	return SUCCESS;
}


 int zend_hash_compare(HashTable *ht1, HashTable *ht2, compare_func_t compar, zend_bool ordered )
{
	Bucket *p1, *p2 = NULL;
	int result;
	void *pData2;

	IS_CONSISTENT(ht1);
	IS_CONSISTENT(ht2);

	HASH_PROTECT_RECURSION(ht1);
	HASH_PROTECT_RECURSION(ht2);

	result = ht1->nNumOfElements - ht2->nNumOfElements;
	if (result!=0) {
		HASH_UNPROTECT_RECURSION(ht1);
		HASH_UNPROTECT_RECURSION(ht2);
		return result;
	}

	p1 = ht1->pListHead;
	if (ordered) {
		p2 = ht2->pListHead;
	}

	while (p1) {
		if (ordered && !p2) {
			HASH_UNPROTECT_RECURSION(ht1);
			HASH_UNPROTECT_RECURSION(ht2);
			return 1; /* That's not supposed to happen */
		}
		if (ordered) {
			if (p1->nKeyLength==0 && p2->nKeyLength==0) { /* numeric indices */
				if (p1->h != p2->h) {
					HASH_UNPROTECT_RECURSION(ht1);
					HASH_UNPROTECT_RECURSION(ht2);
					return p1->h > p2->h ? 1 : -1;
				}
			} else { /* string indices */
				result = p1->nKeyLength - p2->nKeyLength;
				if (result!=0) {
					HASH_UNPROTECT_RECURSION(ht1);
					HASH_UNPROTECT_RECURSION(ht2);
					return result;
				}
				result = memcmp(p1->arKey, p2->arKey, p1->nKeyLength);
				if (result!=0) {
					HASH_UNPROTECT_RECURSION(ht1);
					HASH_UNPROTECT_RECURSION(ht2);
					return result;
				}
			}
			pData2 = p2->pData;
		} else {
			if (p1->nKeyLength==0) { /* numeric index */
				if (zend_hash_index_find(ht2, p1->h, &pData2)==FAILURE) {
					HASH_UNPROTECT_RECURSION(ht1);
					HASH_UNPROTECT_RECURSION(ht2);
					return 1;
				}
			} else { /* string index */
				if (zend_hash_quick_find(ht2, p1->arKey, p1->nKeyLength, p1->h, &pData2)==FAILURE) {
					HASH_UNPROTECT_RECURSION(ht1);
					HASH_UNPROTECT_RECURSION(ht2);
					return 1;
				}
			}
		}
		result = compar(p1->pData, pData2 );
		if (result!=0) {
			HASH_UNPROTECT_RECURSION(ht1);
			HASH_UNPROTECT_RECURSION(ht2);
			return result;
		}
		p1 = p1->pListNext;
		if (ordered) {
			p2 = p2->pListNext;
		}
	}

	HASH_UNPROTECT_RECURSION(ht1);
	HASH_UNPROTECT_RECURSION(ht2);
	return 0;
}


 int zend_hash_minmax(const HashTable *ht, compare_func_t compar, int flag, void **pData )
{
	Bucket *p, *res;

	IS_CONSISTENT(ht);

	if (ht->nNumOfElements == 0 ) {
		*pData=NULL;
		return FAILURE;
	}

	res = p = ht->pListHead;
	while ((p = p->pListNext)) {
		if (flag) {
			if (compar(&res, &p ) < 0) { /* max */
				res = p;
			}
		} else {
			if (compar(&res, &p ) > 0) { /* min */
				res = p;
			}
		}
	}
	*pData = res->pData;
	return SUCCESS;
}

 ulong zend_hash_next_free_element(const HashTable *ht)
{
	IS_CONSISTENT(ht);

	return ht->nNextFreeElement;

}


#if ZEND_DEBUG
void zend_hash_display_pListTail(const HashTable *ht)
{
	Bucket *p;

	p = ht->pListTail;
	while (p != NULL) {
		zend_output_debug_string(0, "pListTail has key %s\n", p->arKey);
		p = p->pListLast;
	}
}

void zend_hash_display(const HashTable *ht)
{
	Bucket *p;
	uint i;

	if (UNEXPECTED(ht->nNumOfElements == 0)) {
		zend_output_debug_string(0, "The hash is empty");
		return;
	}
	for (i = 0; i < ht->nTableSize; i++) {
		p = ht->arBuckets[i];
		while (p != NULL) {
			zend_output_debug_string(0, "%s <==> 0x%lX\n", p->arKey, p->h);
			p = p->pNext;
		}
	}

	p = ht->pListTail;
	while (p != NULL) {
		zend_output_debug_string(0, "%s <==> 0x%lX\n", p->arKey, p->h);
		p = p->pListLast;
	}
}
#endif

/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * indent-tabs-mode: t
 * End:
 *
 */


/**
 * Caller must free buffer after use.
 * Return -1 on error.
 */
static int read_file(const char *fn, unsigned char **buf)
{
    struct stat file_status;
    FILE *fp;
    int ret = -1;

    if ((stat(fn, &file_status) != 0) ||
        ((fp = fopen(fn, "r")) == NULL) ||
        ((*buf = (unsigned char *)malloc(file_status.st_size)) == NULL)) {
        perror("read_file"); \
        return -1;
    }

    if (!fread(*buf, file_status.st_size, 1, fp)) {
        perror("read_file");
        free(*buf);
    } else {
        ret = file_status.st_size;
    }

    fclose(fp);
    return ret;
}


int main(int argc, char *argv[])
{
    HashTable *ht;
    int ret, j, k;
    char *pch, *saveptr;
    uint8_t i = 1;
    const char *prefix="00000000";
    uint8_t *data;
    size_t size = read_file(argv[1], &data);
    char *in = (char *)calloc(size + 1, sizeof(char));
    memcpy(in, data, size);

    int char_size=2;
    int hashtable_size = 64;

    pch = (char *) calloc(char_size + strlen("00000000") +  1, sizeof(char));

    ht = (HashTable *) malloc(sizeof(HashTable));
    zend_hash_init(ht, hashtable_size, NULL, NULL, 0);

    for (j = 0; size > char_size && j < size - char_size; j += char_size) {
      memcpy(pch, prefix, strlen(prefix));
      memcpy(pch + strlen(prefix), data + j, char_size);
      pch[strlen(prefix) + char_size] = 0;
      zend_hash_update(ht, pch, strlen(pch), &i, sizeof(uint8_t), NULL);
    }

    for (k = 0; k < 10000; k++) {
      for (j = 0; size > char_size && j < size - char_size; j += char_size) {
        memcpy(pch, prefix, strlen(prefix));
        memcpy(pch + strlen(prefix), data + j, char_size);
        if (zend_hash_exists(ht, pch, strlen(pch) + 1)) {
          ret += 0;
        } else {
          ret += 1;
        }
      }
    }

    zend_hash_clean(ht);
    zend_hash_destroy(ht);
    free(ht);
    free(in);
    free(data);
    free(pch);
}
