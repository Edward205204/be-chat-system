import {
  MutationCache,
  QueryClient,
  QueryClientProvider
} from '@tanstack/react-query'
import { createRouter, RouterProvider } from '@tanstack/react-router'
import { toast } from 'sonner'
import { ApiError, getErrorMessage } from '@/lib/errors'
import { routeTree } from './routeTree.gen'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // Lỗi 4xx (quyền, không tồn tại) không retry.
      retry: (count, error) =>
        count < 1 &&
        !(error instanceof ApiError && (error.httpStatus ?? 500) < 500)
    }
  },
  // Lỗi mutation toast một chỗ, component không cần tự bắt.
  mutationCache: new MutationCache({
    onError: (error) => toast.error(getErrorMessage(error))
  })
})

const router = createRouter({
  routeTree,
  context: { queryClient },
  defaultPreload: 'intent',
  scrollRestoration: true
})

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}

export default App
